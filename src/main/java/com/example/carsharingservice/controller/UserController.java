package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.UserResponseDto;
import com.example.carsharingservice.dto.UserUpdateRequestDto;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}/role")
    public UserResponseDto updateRole(@PathVariable Long id, @RequestParam User.UserRole role) {
        return userService.updateRole(id, role);
    }

    @GetMapping("/me")
    public UserResponseDto getMyProfile() {
        String mockEmail = "test@example.com";
        return userService.getProfile(mockEmail);
    }

    @PutMapping("/me")
    public UserResponseDto updateMyProfile(@RequestBody @Valid UserUpdateRequestDto requestDto) {
        String mockEmail = "test@example.com";
        return userService.updateProfile(mockEmail, requestDto);
    }
}
