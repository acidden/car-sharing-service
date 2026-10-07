package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.PaymentRequestDto;
import com.example.carsharingservice.dto.PaymentResponseDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.PaymentException;
import com.example.carsharingservice.exception.RentalException;
import com.example.carsharingservice.mapper.PaymentMapper;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.repository.PaymentRepository;
import com.example.carsharingservice.repository.RentalRepository;
import com.example.carsharingservice.service.NotificationService;
import com.example.carsharingservice.service.PaymentService;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final PaymentMapper paymentMapper;
    private final StripeClient stripeClient;
    private final NotificationService notificationService;

    @Value("${carsharing.fine.multiplier}")
    private double fineMultiplier;

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByRentalUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    @Transactional
    public Payment createPaymentSession(
            PaymentRequestDto requestDto,
            String successUrl,
            String cancelUrl
    ) {
        Rental rental = rentalRepository.findById(requestDto.rentalId()).orElseThrow(
                () -> new RentalException("Can't find rental by id: " + requestDto.rentalId()));
        BigDecimal amount = calculateAmount(rental, requestDto.paymentType());
        try {
            long stripeAmount = amount.multiply(new BigDecimal(100)).longValue();
            String paymentName = requestDto.paymentType() == Payment.PaymentType.FINE
                    ? "Fine"
                    : "Rental Payment";
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(stripeAmount)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData
                                                                    .ProductData.builder()
                                                                    .setName(paymentName)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();
            Session session = stripeClient.checkout().sessions().create(params);

            Payment payment = new Payment();
            payment.setRental(rental);
            payment.setAmountToPay(amount);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setType(requestDto.paymentType());
            payment.setSessionId(session.getId());
            payment.setSessionUrl(session.getUrl());
            return paymentRepository.save(payment);
        } catch (StripeException e) {
            throw new PaymentException("Failed to create Stripe checkout session", e);
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto renewPaymentSession(
            Long paymentId,
            String successUrl,
            String cancelUrl
    ) {

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment by id: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.EXPIRED) {
            throw new IllegalStateException("Only EXPIRED payments can be renewed."
                    + " Current status: " + payment.getStatus());
        }

        try {
            String paymentType = payment.getType() == Payment.PaymentType.FINE
                    ? "Car Rental Fine" : "Car Rental Payment";
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                            .setCancelUrl(cancelUrl)
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency("usd")
                                                            .setUnitAmount(payment.getAmountToPay()
                                                                    .multiply(new BigDecimal(100))
                                                                    .longValue())
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem
                                                                            .PriceData.ProductData
                                                                            .builder()
                                                                            .setName(paymentType)
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();
            Session session = stripeClient.checkout().sessions().create(params);

            payment.setSessionId(session.getId());
            payment.setSessionUrl(session.getUrl());
            payment.setStatus(Payment.PaymentStatus.PENDING);

            Payment updatedPayment = paymentRepository.save(payment);
            return paymentMapper.toDto(updatedPayment);

        } catch (com.stripe.exception.StripeException e) {
            throw new RuntimeException("Failed to renew Stripe checkout session", e);
        }
    }

    private BigDecimal calculateAmount(Rental rental, Payment.PaymentType type) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();

        if (type == Payment.PaymentType.PAYMENT) {
            long days = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
            if (days <= 0) {
                days = 1;
            }
            return dailyFee.multiply(new BigDecimal(days));

        } else if (type == Payment.PaymentType.FINE) {
            if (rental.getActualReturnDate() == null) {
                throw new RuntimeException("Car hasn't been returned yet. Cannot calculate fine.");
            }

            long overdueDays = ChronoUnit.DAYS.between(
                    rental.getReturnDate(),
                    rental.getActualReturnDate()
            );
            if (overdueDays <= 0) {
                throw new RuntimeException("No overdue days found for this rental.");
            }

            return dailyFee
                    .multiply(new BigDecimal(overdueDays))
                    .multiply(BigDecimal.valueOf(fineMultiplier));
        }

        throw new IllegalArgumentException("Unknown payment type: " + type);
    }

    @Override
    @Transactional
    public void handleSuccessPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new PaymentException("Payment session not found: " + sessionId));
        payment.setStatus(Payment.PaymentStatus.PAID);
        paymentRepository.save(payment);

        String message = String.format(
                "🔔 *Successful Payment!*\n"
                        + "• *Rental ID:* %d\n"
                        + "• *User ID:* %d\n"
                        + "• *Amount Paid:* $%.2f\n"
                        + "• *Payment Type:* %s",
                payment.getRental().getId(),
                payment.getRental().getUser().getId(),
                payment.getAmountToPay(),
                payment.getType().name()
        );
        notificationService.sendNotification(message);
    }

    @Override
    public String handleCancelPayment() {
        return "Payment was canceled. You can complete the payment later using the session link. "
                + "Note that the session is available for only 24 hours.";
    }

    @Override
    @Transactional
    public void checkExpiredPayments() {
        List<Payment> pendingPayments = paymentRepository
                .findAllByStatus(Payment.PaymentStatus.PENDING);
        for (Payment payment : pendingPayments) {
            try {
                Session session = stripeClient.checkout().sessions()
                        .retrieve(payment.getSessionId());
                if ("expired".equals(session.getStatus())) {
                    payment.setStatus(Payment.PaymentStatus.EXPIRED);
                    paymentRepository.save(payment);
                }
            } catch (Exception e) {
                System.err.println("Failed to check Stripe session status for payment ID "
                        + payment.getId() + " : " + e.getMessage());
            }
        }
    }
}
