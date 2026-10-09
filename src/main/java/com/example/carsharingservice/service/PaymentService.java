package com.example.carsharingservice.service;

import com.example.carsharingservice.dto.PaymentRequestDto;
import com.example.carsharingservice.dto.PaymentResponseDto;
import com.example.carsharingservice.model.Payment;
import java.util.List;

public interface PaymentService {
    List<Payment> getPaymentsByUserId(Long userId);

    List<Payment> getAllPayments();

    Payment createPaymentSession(
            PaymentRequestDto requestDto, String successUrl, String cancelUrl);

    PaymentResponseDto renewPaymentSession(Long paymentId, String successUrl, String cancelUrl);

    void handleSuccessPayment(String sessionId);

    String handleCancelPayment();

    void checkExpiredPayments();
}
