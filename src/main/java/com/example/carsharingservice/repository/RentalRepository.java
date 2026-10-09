package com.example.carsharingservice.repository;

import com.example.carsharingservice.model.Rental;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    @Query("SELECT r FROM Rental r WHERE "
            + "(:userId IS NULL OR r.user.id = :userId) AND "
            + "(:isActive IS NULL OR "
            + "(:isActive = true AND r.actualReturnDate IS NULL) OR "
            + "(:isActive = false AND r.actualReturnDate IS NOT NULL))"
    )
    List<Rental> searchRentals(
            @Param("userId") Long userId,
            @Param("isActive") Boolean isActive
    );

    @Query("SELECT r FROM Rental r "
            + "JOIN FETCH r.user "
            + "JOIN FETCH r.car "
            + "WHERE r.actualReturnDate IS NULL AND "
            + "r.returnDate < :currentDate")
    List<Rental> findAllOverdueRentals(@Param("currentDate") LocalDate currentDate);
}
