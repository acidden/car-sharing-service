package com.example.carsharingservice.mapper;

import com.example.carsharingservice.config.MapperConfig;
import com.example.carsharingservice.dto.PaymentRequestDto;
import com.example.carsharingservice.dto.PaymentResponseDto;
import com.example.carsharingservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    @Mapping(source = "rental.id", target = "rentalId")
    PaymentResponseDto toDto(Payment payment);

    Payment toModel(PaymentRequestDto requestDto);
}

