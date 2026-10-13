package com.example.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.carsharingservice.BaseIntegrationTest;
import com.example.carsharingservice.model.Rental;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Sql(scripts = "classpath:/database/add-all-dependencies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:/database/clear-all-dependencies.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RentalRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("Should find all rentals when search parameters are null")
    void searchRentals_AllParamsNull_ReturnsAllRentals() {
        List<Rental> rentals = rentalRepository.searchRentals(null, null);

        assertEquals(3, rentals.size());
    }

    @Test
    @DisplayName("Should find rentals by specific user ID")
    void searchRentals_ValidUserId_ReturnsUserRentals() {
        List<Rental> userOneRentals = rentalRepository.searchRentals(1L, null);
        List<Rental> userTwoRentals = rentalRepository.searchRentals(2L, null);

        assertEquals(3, userOneRentals.size());
        assertTrue(userTwoRentals.isEmpty());
    }

    @Test
    @DisplayName("Should filter rentals by active status")
    void searchRentals_FilterByActive_ReturnsOnlyActiveRentals() {
        List<Rental> activeRentals = rentalRepository.searchRentals(null, true);

        assertEquals(2, activeRentals.size());
        assertTrue(activeRentals.stream().allMatch(r -> r.getActualReturnDate() == null));
    }

    @Test
    @DisplayName("Should filter rentals by completed status")
    void searchRentals_FilterByCompleted_ReturnsOnlyCompletedRentals() {
        List<Rental> completedRentals = rentalRepository.searchRentals(null, false);

        assertEquals(1, completedRentals.size());
        assertTrue(completedRentals.stream().allMatch(r -> r.getActualReturnDate() != null));
    }

    @Test
    @DisplayName("Should find specific user active rentals")
    void searchRentals_ValidUserIdAndActiveTrue_ReturnsMatchingRentals() {
        List<Rental> result = rentalRepository.searchRentals(1L, true);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should find all overdue rentals based on current date")
    void findAllOverdueRentals_ExistingOverdue_ReturnsOverdueRentalsList() {
        LocalDate today = LocalDate.now();
        List<Rental> overdueRentals = rentalRepository.findAllOverdueRentals(today);

        assertFalse(overdueRentals.isEmpty());
        assertEquals(1, overdueRentals.size());
        assertEquals(3L, overdueRentals.get(0).getId());
    }
}
