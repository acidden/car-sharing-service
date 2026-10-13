package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.PaymentRequestDto;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.service.PaymentService;
import com.example.carsharingservice.service.impl.TelegramNotificationServiceImpl;
import com.stripe.StripeClient;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.service.CheckoutService;
import com.stripe.service.checkout.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        scripts = "classpath:database/clear-all-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "classpath:database/setup-all-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private StripeClient stripeClient;

    @MockitoBean
    private TelegramNotificationServiceImpl telegramNotificationService;

    @Test
    @DisplayName("Verify getPayments() returns only current customer's payments")
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void getPayments_AsCustomer_ReturnsOwnPaymentsOnly() throws Exception {
        mockMvc.perform(get("/payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amountToPay").value(100));
    }

    @Test
    @DisplayName("Verify getPayments() returns all payments for MANAGER when no userId specified")
    @WithMockUser(username = "manager@example.com", roles = {"MANAGER"})
    void getPayments_AsManagerNoFilter_ReturnsAllPayments() throws Exception {
        mockMvc.perform(get("/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].amountToPay").value(100))
                .andExpect(jsonPath("$[1].amountToPay").value(250));
    }

    @Test
    @DisplayName("Verify createPayment() generates Stripe session successfully")
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void createPayment_ValidRequest_ReturnsCreatedPaymentDto() throws Exception {
        PaymentRequestDto requestDto = new PaymentRequestDto(10L, Payment.PaymentType.PAYMENT);

        mockStripeSession("https://stripe.com", "session_mock_123");

        mockMvc.perform(post("/payments").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.sessionUrl").value("https://stripe.com"))
                .andExpect(jsonPath("$.sessionId").value("session_mock_123"));
    }

    @Test
    @DisplayName("Verify renewPayment() session successfully")
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void renewPayment_ValidId_ReturnsRenewedPaymentDto() throws Exception {
        mockStripeSession("https://stripe.com", "session_mock_123");
        mockMvc.perform(post("/payments/103/renew").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(103))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.sessionUrl").value("https://stripe.com"));
    }

    @Test
    @DisplayName("Verify successPayment() handles Stripe redirect successfully")
    void successPayment_ValidSessionId_ReturnsSuccessMessage() throws Exception {
        mockMvc.perform(get("/payments/success").param("session_id", "sess_1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment successful! Thank you."));
    }

    @Test
    @DisplayName("Verify cancelPayment() handles user cancellation")
    void cancelPayment_ReturnsCancelMessage() throws Exception {
        mockMvc.perform(get("/payments/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment was canceled. You can complete the payment" +
                        " later using the session link. Note that the session is available for only 24 hours."));
    }

    private Session mockStripeSession(String url, String sessionId) throws Exception {
        CheckoutService mockCheckoutService = Mockito.mock(CheckoutService.class);
        SessionService mockSessionService = Mockito.mock(SessionService.class);
        Session mockSession = Mockito.mock(Session.class);

        Mockito.when(stripeClient.checkout()).thenReturn(mockCheckoutService);
        Mockito.when(mockCheckoutService.sessions()).thenReturn(mockSessionService);
        Mockito.when(mockSessionService.create(Mockito.any(SessionCreateParams.class)))
                .thenReturn(mockSession);

        Mockito.when(mockSession.getUrl()).thenReturn(url);
        Mockito.when(mockSession.getId()).thenReturn(sessionId);
        Mockito.when(mockSession.getAmountTotal()).thenReturn(10000L);

        return mockSession;
    }
}
