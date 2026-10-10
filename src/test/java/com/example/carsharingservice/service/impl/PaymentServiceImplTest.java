package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.PaymentRequestDto;
import com.example.carsharingservice.dto.PaymentResponseDto;
import com.example.carsharingservice.exception.PaymentException;
import com.example.carsharingservice.exception.RentalException;
import com.example.carsharingservice.mapper.PaymentMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.PaymentRepository;
import com.example.carsharingservice.repository.RentalRepository;
import com.example.carsharingservice.service.NotificationService;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.service.CheckoutService;
import com.stripe.service.checkout.SessionService;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private NotificationService notificationService;

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private StripeClient stripeClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Rental rental;
    private Car car;
    private Payment payment;
    private Session stripeSession;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "fineMultiplier", 2.0);

        User user = new User();
        user.setId(1L);

        car = new Car();
        car.setDailyFee(BigDecimal.valueOf(100));

        rental = new Rental();
        rental.setId(10L);
        rental.setUser(user);
        rental.setCar(car);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(3));

        payment = new Payment();
        payment.setId(100L);
        payment.setRental(rental);
        payment.setAmountToPay(BigDecimal.valueOf(300));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setType(Payment.PaymentType.PAYMENT);
        payment.setSessionId("session_123");

        stripeSession = mock(Session.class);
    }

    @Test
    @DisplayName("Create Session - Should successfully calculate standard rental price "
            + "and create Stripe session")
    void createPaymentSession_StandardPayment_Success() throws StripeException {
        PaymentRequestDto requestDto = new PaymentRequestDto(10L, Payment.PaymentType.PAYMENT);
        when(stripeSession.getId()).thenReturn("session_123");
        when(stripeSession.getUrl()).thenReturn("https://stripe.com");
        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));
        when(stripeClient.checkout().sessions().create(any(SessionCreateParams.class)))
                .thenReturn(stripeSession);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.createPaymentSession(
                requestDto, "http://success", "http://cancel"
        );

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(300), result.getAmountToPay());
        assertEquals(Payment.PaymentStatus.PENDING, result.getStatus());
        assertEquals("session_123", result.getSessionId());
    }

    @Test
    @DisplayName("Create Session - Should successfully calculate fine for overdue days")
    void createPaymentSession_FinePayment_Success() throws StripeException {
        PaymentRequestDto requestDto = new PaymentRequestDto(10L, Payment.PaymentType.FINE);
        when(stripeSession.getId()).thenReturn("session_123");
        when(stripeSession.getUrl()).thenReturn("https://stripe.com");
        rental.setActualReturnDate(rental.getReturnDate().plusDays(2));

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));
        when(stripeClient.checkout().sessions().create(any(SessionCreateParams.class)))
                .thenReturn(stripeSession);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.createPaymentSession(
                requestDto, "http://success", "http://cancel"
        );

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(400.0), result.getAmountToPay());
        assertEquals(Payment.PaymentType.FINE, result.getType());
    }

    @Test
    @DisplayName("Create Session - Should throw RentalException when rental not found")
    void createPaymentSession_RentalNotFound_ThrowsRentalException() {
        PaymentRequestDto requestDto = new PaymentRequestDto(99L, Payment.PaymentType.PAYMENT);
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RentalException.class, () ->
                paymentService.createPaymentSession(requestDto, "http://success", "http://cancel")
        );
    }

    @Test
    @DisplayName("Get Payments By User ID - Should return list of payments")
    void getPaymentsByUserId_ValidId_ReturnsList() {
        when(paymentRepository.findByRentalUserId(1L)).thenReturn(List.of(payment));

        List<Payment> actual = paymentService.getPaymentsByUserId(1L);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        verify(paymentRepository).findByRentalUserId(1L);
    }

    @Test
    @DisplayName("Get All Payments - Should return list of all payments")
    void getAllPayments_ReturnsList() {
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        List<Payment> actual = paymentService.getAllPayments();

        assertNotNull(actual);
        assertEquals(1, actual.size());
        verify(paymentRepository).findAll();
    }

    @Test
    @DisplayName("Handle Cancel Payment - Should return informative string message")
    void handleCancelPayment_ReturnsCancelMessage() {
        String result = paymentService.handleCancelPayment();

        assertNotNull(result);
        assertTrue(result.contains("canceled"));
    }

    @Test
    @DisplayName("Renew Session - Should successfully recreate Stripe session for EXPIRED payment")
    void renewPaymentSession_ExpiredStatus_Success() throws StripeException {
        Long paymentId = 100L;
        payment.setStatus(Payment.PaymentStatus.EXPIRED);

        SessionService mockSessionService = mock(SessionService.class);
        CheckoutService mockCheckoutService = mock(CheckoutService.class);
        when(stripeClient.checkout()).thenReturn(mockCheckoutService);
        when(mockCheckoutService.sessions()).thenReturn(mockSessionService);
        when(mockSessionService.create(any(SessionCreateParams.class))).thenReturn(stripeSession);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponseDto expectedDto = new PaymentResponseDto(
                100L,
                "PENDING",
                "PAYMENT",
                "https://stripe.com",
                "session_123",
                BigDecimal.valueOf(300),
                10L
        );
        when(paymentMapper.toDto(payment)).thenReturn(expectedDto);

        PaymentResponseDto result = paymentService.renewPaymentSession(
                paymentId, "http://success", "http://cancel"
        );

        assertNotNull(result);
        assertEquals(Payment.PaymentStatus.PENDING, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Renew Session - Should throw PaymentException when payment status is not EXPIRED")
    void renewPaymentSession_NotExpiredStatus_ThrowsPaymentException() {
        Long paymentId = 100L;
        payment.setStatus(Payment.PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(PaymentException.class, () ->
                paymentService.renewPaymentSession(paymentId, "http://success", "http://cancel")
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Handle Success - Should update payment status and send Telegram notification")
    void handleSuccessPayment_ValidSession_UpdatesStatusAndSendsNotification() {
        String sessionId = "session_123";
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        paymentService.handleSuccessPayment(sessionId);

        assertEquals(Payment.PaymentStatus.PAID, payment.getStatus());
        verify(paymentRepository).save(payment);
        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @DisplayName("Check Expired - Should change status to EXPIRED if Stripe session is expired")
    void checkExpiredPayments_ExpiredStripeSession_UpdatesStatus() throws StripeException {
        when(paymentRepository.findAllByStatus(Payment.PaymentStatus.PENDING))
                .thenReturn(List.of(payment));

        Session mockRetrievedSession = mock(Session.class);
        when(mockRetrievedSession.getStatus()).thenReturn("expired");
        when(stripeClient.checkout().sessions().retrieve("session_123"))
                .thenReturn(mockRetrievedSession);

        paymentService.checkExpiredPayments();

        assertEquals(Payment.PaymentStatus.EXPIRED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }
}
