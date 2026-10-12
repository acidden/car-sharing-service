package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.UserLoginRequestDto;
import com.example.carsharingservice.dto.UserRegistrationRequestDto;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Sql(
            scripts = "classpath:database/clear-all-test-data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/setup-all-test-data.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    @DisplayName("Verify register() endpoint creates a new user successfully")
    void register_ValidRequest_ReturnsCreatedUserDto() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "newuser@example.com",
                "John",
                "Doe",
                "password123",
                "password123"
        );
        mockMvc.perform(post("/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    @DisplayName("Verify register() returns error status when email already exists")
    void register_DuplicateEmail_ReturnsBadRequestOrConflict() throws Exception {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "existinguser@example.com",
                "John",
                "Doe",
                "password123",
                "password123"
        );

        mockMvc.perform(post("/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Verify login() endpoint returns JWT token for valid credentials")
    void login_ValidCredentials_ReturnsJwtToken() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "existinguser@example.com",
                "password123"
        );
        mockMvc.perform(post("/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
