package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.UserRegistrationRequestDto;
import com.example.carsharingservice.dto.UserResponseDto;
import com.example.carsharingservice.dto.UserUpdateRequestDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.RegistrationException;
import com.example.carsharingservice.mapper.UserMapper;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRegistrationRequestDto registrationRequestDto;
    private UserUpdateRequestDto updateRequestDto;
    private UserResponseDto responseDto;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail(userEmail);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("securePassword123");
        user.setRole(User.UserRole.CUSTOMER);

        registrationRequestDto = new UserRegistrationRequestDto(
                userEmail,
                "John",
                "Doe",
                "securePassword123",
                "securePassword123"
        );

        updateRequestDto = new UserUpdateRequestDto(
                "Johnny",
                "Updated"
        );

        responseDto = new UserResponseDto(
                1L,
                userEmail,
                "John",
                "Doe",
                User.UserRole.CUSTOMER
        );
    }

    @Test
    @DisplayName("Register - Should successfully register a new customer")
    void register_ValidRequest_ReturnsUserResponseDto() {
        when(userRepository.findByEmail(registrationRequestDto.email()))
                .thenReturn(Optional.empty());
        when(userMapper.toModel(registrationRequestDto)).thenReturn(user);
        when(passwordEncoder.encode(registrationRequestDto.password()))
                .thenReturn("encodedPassword123");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto actualResponse = userService.register(registrationRequestDto);

        assertNotNull(actualResponse);
        assertEquals(responseDto, actualResponse);
        assertEquals(User.UserRole.CUSTOMER, user.getRole());
        assertEquals("encodedPassword123", user.getPassword());

        verify(userRepository).findByEmail(userEmail);
        verify(userMapper).toModel(registrationRequestDto);
        verify(passwordEncoder).encode("securePassword123");
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Register - Should throw RegistrationException when email already exists")
    void register_EmailAlreadyExists_ThrowsRegistrationException() {
        when(userRepository.findByEmail(registrationRequestDto.email()))
                .thenReturn(Optional.of(user));
        RegistrationException exception = assertThrows(RegistrationException.class, () ->
                userService.register(registrationRequestDto)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(passwordEncoder, userMapper);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Get Profile - Should return profile details when user exists")
    void getProfile_ExistingEmail_ReturnsUserResponseDto() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto actualResponse = userService.getProfile(userEmail);

        assertNotNull(actualResponse);
        assertEquals(userEmail, actualResponse.email());
        verify(userRepository).findByEmail(userEmail);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Get Profile - Should throw EntityNotFoundException when user not found")
    void getProfile_NonExistingEmail_ThrowsEntityNotFoundException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                userService.getProfile(userEmail)
        );

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Update Profile - Should successfully update info and return profile")
    void updateProfile_ExistingEmail_ReturnsUpdatedUserResponseDto() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        doAnswer(invocation -> {
            User targetUser = invocation.getArgument(1);
            targetUser.setFirstName(updateRequestDto.firstName());
            targetUser.setLastName(updateRequestDto.lastName());
            return null;
        }).when(userMapper).updateEntityFromDto(updateRequestDto, user);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail(userEmail);
        updatedUser.setFirstName("Johnny");
        updatedUser.setLastName("Updated");
        updatedUser.setRole(User.UserRole.CUSTOMER);

        UserResponseDto updatedResponseDto = new UserResponseDto(
                1L, userEmail, "Johnny", "Updated", User.UserRole.CUSTOMER
        );

        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedResponseDto);

        UserResponseDto actualResponse = userService.updateProfile(userEmail, updateRequestDto);

        assertNotNull(actualResponse);
        assertEquals("Johnny", actualResponse.firstName());
        assertEquals("Updated", actualResponse.lastName());

        verify(userRepository).findByEmail(userEmail);
        verify(userMapper).updateEntityFromDto(updateRequestDto, user);
        verify(userRepository).save(user);
        verify(userMapper).toDto(updatedUser);
    }

    @Test
    @DisplayName("Update Profile - Should throw EntityNotFoundException when profile not found")
    void updateProfile_NonExistingEmail_ThrowsEntityNotFoundException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                userService.updateProfile(userEmail, updateRequestDto)
        );

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(userMapper);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Update Role - Should successfully change role of user")
    void updateRole_ExistingId_ReturnsUpdatedUserResponseDto() {
        Long userId = 1L;
        User.UserRole newRole = User.UserRole.MANAGER;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponseDto roleUpdatedResponseDto = new UserResponseDto(
                1L, userEmail, "John", "Doe", User.UserRole.MANAGER
        );
        when(userMapper.toDto(user)).thenReturn(roleUpdatedResponseDto);

        UserResponseDto actualResponse = userService.updateRole(userId, newRole);

        assertNotNull(actualResponse);
        assertEquals(User.UserRole.MANAGER, actualResponse.role());
        assertEquals(User.UserRole.MANAGER, user.getRole());

        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Update Role - Should throw EntityNotFoundException when user ID not found")
    void updateRole_NonExistingId_ThrowsEntityNotFoundException() {
        Long userId = 99L;
        User.UserRole newRole = User.UserRole.MANAGER;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                userService.updateRole(userId, newRole)
        );

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(userMapper);
    }
}
