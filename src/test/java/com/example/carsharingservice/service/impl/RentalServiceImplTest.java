package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.CarResponseDto;
import com.example.carsharingservice.dto.RentalRequestDto;
import com.example.carsharingservice.dto.RentalResponseDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.RentalException;
import com.example.carsharingservice.mapper.RentalMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.CarRepository;
import com.example.carsharingservice.repository.PaymentRepository;
import com.example.carsharingservice.repository.RentalRepository;
import com.example.carsharingservice.repository.UserRepository;
import com.example.carsharingservice.service.NotificationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private User customerUser;
    private User adminUser;
    private Car car;
    private Rental rental;
    private RentalRequestDto requestDto;
    private RentalResponseDto responseDto;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        customerUser = new User();
        customerUser.setId(1L);
        customerUser.setEmail(userEmail);
        customerUser.setRole(User.UserRole.CUSTOMER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(User.UserRole.MANAGER);

        car = new Car();
        car.setId(10L);
        car.setBrand("Tesla");
        car.setModel("Model 3");
        car.setInventory(2);

        rental = new Rental();
        rental.setId(100L);
        rental.setUser(customerUser);
        rental.setCar(car);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(3));

        requestDto = new RentalRequestDto(
                10L,
                LocalDate.now().plusDays(3)
        );
        CarResponseDto carResponseDto = new CarResponseDto(
                10L,
                "Model 3",
                "Tesla",
                Car.CarType.SEDAN,
                2,
                BigDecimal.valueOf(100)
        );
        responseDto = new RentalResponseDto(
                100L,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                null,
                carResponseDto,
                1L
        );
    }

    @Test
    @DisplayName("Save rental - Should successfully create rental when valid request")
    void save_ValidRequest_Success() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(customerUser));
        when(paymentRepository.existsByRentalUserIdAndStatus(customerUser.getId(),
                Payment.PaymentStatus.PENDING)).thenReturn(false);
        when(carRepository.findById(requestDto.carId())).thenReturn(Optional.of(car));
        when(rentalMapper.toModel(requestDto)).thenReturn(rental);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        RentalResponseDto result = rentalService.save(requestDto, userEmail);

        assertNotNull(result);
        assertEquals(1, car.getInventory());
        verify(notificationService).sendNotification(anyString());
        verify(rentalRepository).save(rental);
    }

    @Test
    @DisplayName("Save rental - Should throw RentalException when user has pending payments")
    void save_PendingPaymentsExist_ThrowsRentalException() {

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(customerUser));
        when(paymentRepository.existsByRentalUserIdAndStatus(customerUser.getId(),
                Payment.PaymentStatus.PENDING)).thenReturn(true);

        assertThrows(RentalException.class, () -> rentalService.save(requestDto, userEmail));
        verifyNoInteractions(carRepository, rentalMapper, rentalRepository, notificationService);
    }

    @Test
    @DisplayName("Save rental - Should throw RuntimeException when car inventory is 0")
    void save_CarInventoryZero_ThrowsRentalException() {

        car.setInventory(0);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(customerUser));
        when(paymentRepository.existsByRentalUserIdAndStatus(customerUser.getId(),
                Payment.PaymentStatus.PENDING)).thenReturn(false);
        when(carRepository.findById(requestDto.carId())).thenReturn(Optional.of(car));

        RentalException exception = assertThrows(RentalException.class,
                () -> rentalService.save(requestDto, userEmail));
        assertEquals("Car is not available for rental. Inventory is 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Search rentals - Should enforce customer ID when user role is CUSTOMER")
    void search_UserIsCustomer_OverridesUserId() {

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(customerUser));
        when(rentalRepository.searchRentals(customerUser.getId(), true))
                .thenReturn(List.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        List<RentalResponseDto> result = rentalService.search(999L, true, userEmail);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(rentalRepository).searchRentals(customerUser.getId(), true);
    }

    @Test
    @DisplayName("Find by ID - Should throw AccessDeniedException "
            + "when customer accesses someone else's rental")
    void findById_CustomerAccessingOthersRental_ThrowsAccessDeniedException() {

        User anotherCustomer = new User();
        anotherCustomer.setId(55L);
        rental.setUser(anotherCustomer);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(customerUser));

        assertThrows(AccessDeniedException.class, () -> rentalService.findById(100L, userEmail));
    }

    @Test
    @DisplayName("Return car - Should successfully return car,"
            + " increase inventory and send notification")
    void returnCar_ValidRental_Success() {

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(customerUser));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        int initialInventory = car.getInventory();

        RentalResponseDto result = rentalService.returnCar(100L, userEmail);

        assertNotNull(result);
        assertEquals(initialInventory + 1, car.getInventory());
        assertNotNull(rental.getActualReturnDate());
        assertEquals(LocalDate.now(), rental.getActualReturnDate());
        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @DisplayName("Return car - Should throw RentalException when car already returned")
    void returnCar_AlreadyReturned_ThrowsRentalException() {

        rental.setActualReturnDate(LocalDate.now().minusDays(1));
        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(customerUser));

        assertThrows(RentalException.class, () -> rentalService.returnCar(100L, userEmail));
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Return car - Manager should successfully return other customer`s car")
    void returnCar_ManagerAccessingOthersRental_SuccessReturn() {
        Long rentalId = 100L;
        rental.setUser(customerUser);

        String adminEmail = "admin@example.com";
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        RentalResponseDto result = rentalService.returnCar(rentalId, adminEmail);

        assertNotNull(result);

    }

    @Test
    @DisplayName("Find by ID - Should throw EntityNotFoundException when rental not found")
    void findById_RentalNotFound_ThrowsEntityNotFoundException() {
        Long nonExistentRentalId = 999L;
        when(rentalRepository.findById(nonExistentRentalId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.findById(nonExistentRentalId, userEmail));

        assertEquals("Can`t find rental by ID: " + nonExistentRentalId, exception.getMessage());
        verifyNoInteractions(userRepository, rentalMapper);
    }

    @Test
    @DisplayName("Find by ID - Should throw EntityNotFoundException when user not found")
    void findById_UserNotFound_ThrowsEntityNotFoundException() {
        Long rentalId = 100L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.findById(rentalId, userEmail));

        assertEquals("Can't find user by email: " + userEmail, exception.getMessage());
        verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Find by ID - Should successfully return rental DTO "
            + "when customer accesses their own rental")
    void findById_CustomerAccessingOwnRental_Success() {
        Long rentalId = 100L;

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(customerUser));
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        RentalResponseDto result = rentalService.findById(rentalId, userEmail);

        assertNotNull(result);
        assertEquals(responseDto, result);
        verify(rentalMapper).toDto(rental);
    }

    @Test
    @DisplayName("Find by ID - Should successfully return rental DTO "
            + "when manager accesses someone else's rental")
    void findById_ManagerAccessingOthersRental_Success() {
        Long rentalId = 100L;
        rental.setUser(customerUser);

        String adminEmail = "admin@example.com";
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        RentalResponseDto result = rentalService.findById(rentalId, adminEmail);

        assertNotNull(result);
        assertEquals(responseDto, result);
        verify(rentalMapper).toDto(rental);
    }
}

