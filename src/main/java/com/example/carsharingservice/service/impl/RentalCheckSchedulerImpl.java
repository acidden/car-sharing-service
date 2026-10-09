package com.example.carsharingservice.service.impl;

import static java.time.temporal.ChronoUnit.DAYS;

import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.repository.RentalRepository;
import com.example.carsharingservice.service.NotificationService;
import com.example.carsharingservice.service.RentalCheckScheduler;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalCheckSchedulerImpl implements RentalCheckScheduler {
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Override
    @Scheduled(cron = "0 0 8 * * *")
    public void checkOverdueRentals() {
        LocalDate today = LocalDate.now();
        List<Rental> overdueRentals = rentalRepository.findAllOverdueRentals(today);

        if (overdueRentals.isEmpty()) {
            notificationService.sendNotification("No rentals overdue today!");
            return;
        }
        for (Rental rental : overdueRentals) {
            String message = String.format(
                    "⚠️  OVERDUE RENTAL ALERT! %n"
                            + "👤  User: %s%n"
                            + "🚘  Car: %s %s%n"
                            + "📅  Expected Return Date: %s%n"
                            + "⏳  Days Overdue: %d",
                    rental.getUser().getEmail(),
                    rental.getCar().getBrand(),
                    rental.getCar().getModel(),
                    rental.getReturnDate(),
                    DAYS.between(rental.getReturnDate(), today)
            );
            notificationService.sendNotification(message);
        }
    }
}
