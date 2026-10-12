package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.RentalRequestDto;
import com.example.carsharingservice.service.NotificationService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(
        scripts = "classpath:database/clear-all-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "classpath:database/setup-all-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class RentalsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Verify createRental() successfully rents an available car")
    @WithMockUser(username = "other_customer@example.com", roles = {"CUSTOMER"})
    void createRental_ValidRequest_ReturnsCreatedRentalDto() throws Exception {
        RentalRequestDto requestDto = new RentalRequestDto(1L, LocalDate.now().plusDays(5));

        Mockito.doNothing().when(notificationService).sendNotification(Mockito.anyString());

        mockMvc.perform(post("/rentals").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.car.id").value(1));
    }

    @Test
    @DisplayName("Verify getRentals() returns only current customer's own rentals")
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void getRentals_AsCustomer_ReturnsOwnRentalsOnly() throws Exception {
        mockMvc.perform(get("/rentals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @DisplayName("Verify getRentals() allows MANAGER to filter by any user id and status")
    @WithMockUser(username = "manager@example.com", roles = {"MANAGER"})
    void getRentals_AsManagerWithFilter_ReturnsFilteredRentals() throws Exception {
        mockMvc.perform(get("/rentals?user_id=3&is_active=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(11));
    }

    @Test
    @DisplayName("Verify getRentalById() returns specific rental details")
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void getRentalById_ValidId_ReturnsRentalDto() throws Exception {
        mockMvc.perform(get("/rentals/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.car").exists());
    }

    @Test
    @DisplayName("Verify returnCar() successfully updates actual return date")
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void returnCar_ValidId_ReturnsUpdatedRentalDto() throws Exception {
        mockMvc.perform(post("/rentals/10/return").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.actualReturnDate").exists());
    }
}
