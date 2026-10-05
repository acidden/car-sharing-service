package com.example.carsharingservice.mapper;

import com.example.carsharingservice.config.MapperConfig;
import com.example.carsharingservice.dto.RentalRequestDto;
import com.example.carsharingservice.dto.RentalResponseDto;
import com.example.carsharingservice.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = CarMapper.class)
public interface RentalMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "actualReturnDate", ignore = true)
    @Mapping(target = "rentalDate", expression = "java(java.time.LocalDate.now())")
    Rental toModel(RentalRequestDto requestDto);

    @Mapping(target = "userId", source = "user.id")
    RentalResponseDto toDto(Rental rental);
}
