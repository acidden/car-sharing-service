package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.UserResponseDto;
import com.example.carsharingservice.dto.UserUpdateRequestDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.RegistrationException;
import com.example.carsharingservice.mapper.UserMapper;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.UserRepository;
import com.example.carsharingservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new RegistrationException("Unable to complete registration. User with email: "
            + requestDto.email() + " already exists.");
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        user.setRole(User.UserRole.CUSTOMER);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto getProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can`t find user by email: " + email));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateProfile(String email, UserUpdateRequestDto requestDto) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can`t find user by email: " + email));
        userMapper.updateEntityFromDto(requestDto, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateRole(Long id, User.UserRole role) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find user by ID: " + id));
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }
}
