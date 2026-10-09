package com.example.carsharingservice.repository;

import com.example.carsharingservice.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySessionId(String stripeSessionId);

    @Query("SELECT p FROM Payment p WHERE p.rental.user.id = :userId")
    List<Payment> findByRentalUserId(@Param("userId") Long userId);

    boolean existsByRentalUserIdAndStatus(Long userId, Payment.PaymentStatus status);

    List<Payment> findAllByStatus(Payment.PaymentStatus status);
}
