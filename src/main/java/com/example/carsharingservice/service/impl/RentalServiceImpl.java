package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.RentalRequestDto;
import com.example.carsharingservice.dto.RentalResponseDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.exception.RentalException;
import com.example.carsharingservice.mapper.RentalMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.CarRepository;
import com.example.carsharingservice.repository.RentalRepository;
import com.example.carsharingservice.repository.UserRepository;
import com.example.carsharingservice.service.NotificationService;
import com.example.carsharingservice.service.RentalService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RentalResponseDto save(RentalRequestDto requestDto, String userEmail) {
        Car car = carRepository.findById(requestDto.carId()).orElseThrow(
                () -> new EntityNotFoundException("Can`t find car by ID: " + requestDto.carId()));
        if (car.getInventory() <= 0) {
            throw new RuntimeException("Car is not available for rental. Inventory is 0.");
        }
        User user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by email: " + userEmail)
        );
        car.setInventory(car.getInventory() - 1);
        Rental rental = rentalMapper.toModel(requestDto);
        rental.setUser(user);
        rental.setCar(car);

        Rental savedRental = rentalRepository.save(rental);
        String message = String.format(
                "🚗  New Rental Created! %n"
                        + "👤  User:  %s%n"
                        + "🚘  Car:  %s %s%n"
                        + "📅  Expected Return Date:  %s",
                user.getEmail(), car.getBrand(), car.getModel(), rental.getReturnDate()
        );
        notificationService.sendNotification(message);
        return rentalMapper.toDto(savedRental);
    }

    @Override
    public List<RentalResponseDto> search(Long userId, Boolean isActive, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by email: " + userEmail)
        );
        if (currentUser.getRole() == User.UserRole.CUSTOMER) {
            userId = currentUser.getId();
        }
        return rentalRepository.searchRentals(userId, isActive).stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public RentalResponseDto findById(Long id, String userEmail) {
        Rental rental = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find rental by ID: " + id)
        );
        User currentUser = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by email: " + userEmail)
        );
        if (currentUser.getRole() == User.UserRole.CUSTOMER
                && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don`t have permission to view this rental");
        }
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public RentalResponseDto returnCar(Long id, String userEmail) {
        Rental rental = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find rental by ID: " + id)
        );
        User currentUser = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by email: " + userEmail)
        );
        if (currentUser.getRole() == User.UserRole.CUSTOMER
                && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to return this car.");
        }
        if (rental.getActualReturnDate() != null) {
            throw new RentalException("Car has already been returned for this rental.");
        }
        rental.setActualReturnDate(LocalDate.now());
        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        Rental updatedRental = rentalRepository.save(rental);
        return rentalMapper.toDto(updatedRental);
    }
}
