package com.example.carsharingservice.repository;

import com.example.carsharingservice.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySessionId(String stripeSessionId);

    List<Payment> findByRentalId(Long userId);
}
