package com.example.carsharingservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeSessionExpirationScheduler {
    private final PaymentService paymentService;

    @Scheduled(cron = "0 * * * * *")
    public void checkExpiredSessions() {
        paymentService.checkExpiredPayments();
    }
}
