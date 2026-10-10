package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.RentalRepository;
import com.example.carsharingservice.service.NotificationService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentalCheckSchedulerImplTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RentalCheckSchedulerImpl rentalCheckScheduler;

    private Rental overdueRental;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("customer@example.com");

        Car car = new Car();
        car.setBrand("Tesla");
        car.setModel("Model 3");

        overdueRental = new Rental();
        overdueRental.setUser(user);
        overdueRental.setCar(car);
        overdueRental.setReturnDate(LocalDate.now().minusDays(2));
    }

    @Test
    @DisplayName("Check Overdue - Should send global info message when no rentals are overdue")
    void checkOverdueRentals_NoOverdue_SendsNoRentalsOverdueMessage() {
        when(rentalRepository.findAllOverdueRentals(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        rentalCheckScheduler.checkOverdueRentals();

        verify(notificationService).sendNotification("No rentals overdue today!");
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Check Overdue - Should send detailed alert message for each overdue rental")
    void checkOverdueRentals_WithOverdueRentals_SendsDetailedAlerts() {
        when(rentalRepository.findAllOverdueRentals(any(LocalDate.class)))
                .thenReturn(List.of(overdueRental));

        rentalCheckScheduler.checkOverdueRentals();

        verify(notificationService).sendNotification(contains("⚠️  OVERDUE RENTAL ALERT!"));
        verify(notificationService).sendNotification(contains("customer@example.com"));
        verify(notificationService).sendNotification(contains("Tesla Model 3"));
        verify(notificationService).sendNotification(contains("Days Overdue: 2"));

        verify(notificationService).sendNotification(anyString());
    }
}
