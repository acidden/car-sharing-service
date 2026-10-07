package com.example.carsharingservice.dto;

import java.math.BigDecimal;

public record PaymentResponseDto(
        Long id,
        String status,
        String type,
        String sessionUrl,
        String sessionId,
        BigDecimal amountToPay,
        Long rentalId
) {}
