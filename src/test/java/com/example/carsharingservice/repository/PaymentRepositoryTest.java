package com.example.carsharingservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.carsharingservice.BaseIntegrationTest;
import com.example.carsharingservice.model.Payment;
import java.util.List;
import java.util.Optional;
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
public class PaymentRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Should find payment by Stripe session ID")
    void findBySessionId_ValidSessionId_ReturnsPayment() {
        Optional<Payment> foundPayment = paymentRepository.findBySessionId("test_session_123_xyz");

        assertTrue(foundPayment.isPresent());
        assertEquals("test_session_123_xyz", foundPayment.get().getSessionId());
    }

    @Test
    @DisplayName("Should find payments by rental user ID")
    void findByRentalUserId_ExistingUserId_ReturnsPaymentList() {
        List<Payment> payments = paymentRepository.findByRentalUserId(1L);

        assertFalse(payments.isEmpty());
        assertEquals(2, payments.size());
        assertEquals(1L, payments.get(0).getRental().getId());
    }

    @Test
    @DisplayName("Should return true if unpaid payment exists for user")
    void existsByRentalUserIdAndStatus_ExistingUnpaid_ReturnsTrue() {
        boolean exists = paymentRepository.existsByRentalUserIdAndStatus(1L, Payment.PaymentStatus.PENDING);

        assertTrue(exists);
    }
}
