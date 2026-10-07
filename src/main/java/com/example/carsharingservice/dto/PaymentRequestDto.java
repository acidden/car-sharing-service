package com.example.carsharingservice.dto;

import com.example.carsharingservice.model.Payment;

public record PaymentRequestDto(
        Long rentalId,
        Payment.PaymentType paymentType
) {}
