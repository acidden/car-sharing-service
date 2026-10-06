package com.example.carsharingservice.service;

import com.example.carsharingservice.model.Payment;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    public List<Payment> getPaymentsByUserId(Long userId);

    public String createPaymentSession(
            Long userId, BigDecimal amount, String successUrl, String cancelUrl);
}
