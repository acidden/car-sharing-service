package com.example.carsharingservice.service;

import com.example.carsharingservice.dto.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.UserResponseDto;
import com.example.carsharingservice.dto.UserUpdateRequestDto;
import com.example.carsharingservice.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto);

    UserResponseDto getProfile(String email);

    UserResponseDto updateProfile(String email, UserUpdateRequestDto requestDto);

    UserResponseDto updateRole(Long id, User.UserRole role);
}
