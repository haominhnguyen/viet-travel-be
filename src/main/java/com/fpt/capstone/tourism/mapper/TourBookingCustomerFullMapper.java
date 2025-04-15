package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourBookingCustomerDTO;
import com.fpt.capstone.tourism.model.TourBookingCustomer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourBookingCustomerFullMapper {
    // Convert Entity to DTO
    TourBookingCustomerDTO toDto(TourBookingCustomer entity);
}
