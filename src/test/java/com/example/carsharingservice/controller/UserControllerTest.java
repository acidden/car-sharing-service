package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.UserUpdateRequestDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Verify getMyProfile() returns profile of currently logged-in user")
    @WithMockUser(username = "profileuser@example.com", roles = {"CUSTOMER"})
    void getMyProfile_AuthenticatedUser_ReturnsUserDto() throws Exception {
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("profileuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("Alex"));
    }

    @Test
    @DisplayName("Verify updateMyProfile() successfully edits user's names")
    @WithMockUser(username = "profileuser@example.com", roles = {"CUSTOMER"})
    void updateMyProfile_ValidRequest_ReturnsUpdatedUserDto() throws Exception {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto("Alex-Updated", "Smith-Updated");

        mockMvc.perform(patch("/users/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alex-Updated"))
                .andExpect(jsonPath("$.lastName").value("Smith-Updated"));
    }

    @Test
    @DisplayName("Verify updateRole() allows MANAGER to promote user role")
    @WithMockUser(username = "manageruser@example.com", roles = {"MANAGER"})
    void updateRole_AsManager_SuccessfullyChangesRole() throws Exception {
        mockMvc.perform(put("/users/10/role")
                        .param("role", "MANAGER") // Передаем Enum UserRole как строку
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }
}
