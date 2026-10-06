package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.exception.PaymentException;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.repository.PaymentRepository;
import com.example.carsharingservice.repository.RentalRepository;
import com.example.carsharingservice.service.PaymentService;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final StripeClient stripeClient;

    @Override
    @Transactional
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByRentalId(userId);
    }

    @Override
    @Transactional
    public String createPaymentSession(
            Long rentalId,
            BigDecimal amount,
            String successUrl,
            String cancelUrl
    ) {
        try {
            long stripeAmount = amount.multiply(new BigDecimal(100)).longValue();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("eur")
                                                    .setUnitAmount(stripeAmount)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData
                                                                    .ProductData.builder()
                                                                    .setName("Car Sharing Payment")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();
            Session session = stripeClient.checkout().sessions().create(params);

            Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                    () -> new RuntimeException("Can't find rental by id: " + rentalId));
            Payment payment = new Payment();
            payment.setRental(rental);
            payment.setAmountToPay(amount);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setSessionId(session.getId());
            paymentRepository.save(payment);
            return session.getUrl();
        } catch (StripeException e) {
            throw new PaymentException("Failed to create Stripe checkout session", e);
        }
    }
}
