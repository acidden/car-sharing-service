package com.example.carsharingservice.repository;

import com.example.carsharingservice.BaseIntegrationTest;
import com.example.carsharingservice.model.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by explicit email if user exists")
    void findByEmail_ExistingEmail_ReturnsUser() {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("securePassword123");
        user.setRole(User.UserRole.CUSTOMER);

        userRepository.save(user);

        Optional<User> foundUserOptional = userRepository.findByEmail("testuser@example.com");

        assertTrue(foundUserOptional.isPresent(), "User should be found by email");
        User foundUser = foundUserOptional.get();
        assertEquals("testuser@example.com", foundUser.getEmail());
        assertEquals("John", foundUser.getFirstName());
        assertEquals("Doe", foundUser.getLastName());
    }

    @Test
    @DisplayName("Should return empty Optional when searching for non-existing email")
    void findByEmail_NonExistingEmail_ReturnsEmptyOptional() {
        Optional<User> foundUserOptional = userRepository.findByEmail("nonexistingemail@example.com");

        assertTrue(foundUserOptional.isEmpty(), "Optional should be empty for non-existing email");
    }
}
