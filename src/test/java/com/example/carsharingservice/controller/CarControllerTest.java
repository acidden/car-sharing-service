package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.CarRequestDto;
import com.example.carsharingservice.model.Car;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
public class CarControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Verify createCar() endpoint adds a new car successfully")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void createCar_ValidRequest_ReturnsCreatedCarDto() throws Exception {
        CarRequestDto requestDto = new CarRequestDto(
                "Model S",
                "Tesla",
                Car.CarType.SEDAN,
                5,
                BigDecimal.valueOf(150.0)
        );
        mockMvc.perform(post("/cars").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // Ожидаем 201 Created
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.model").value("Model S"));
    }

    @Test
    @DisplayName("Verify getAllCars() returns a paginated list of cars")
    @WithMockUser(username = "user", roles = {"CUSTOMER"})
    void getAllCars_ValidRequest_ReturnsCarsList() throws Exception {
        mockMvc.perform(get("/cars?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].model").value("Camry"));
    }

    @Test
    @DisplayName("Verify updateCar() updates car details successfully")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void updateCar_ValidRequest_ReturnsUpdatedCarDto() throws Exception {
        CarRequestDto requestDto = new CarRequestDto(
                "Camry Updated",
                "Toyota",
                Car.CarType.SEDAN,
                3,
                BigDecimal.valueOf(90.0)
        );
        mockMvc.perform(put("/cars/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Camry Updated"));
    }

    @Test
    @DisplayName("Verify deleteCar() performs soft-delete successfully")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void deleteCar_ExistingId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/cars/1").with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/cars/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
