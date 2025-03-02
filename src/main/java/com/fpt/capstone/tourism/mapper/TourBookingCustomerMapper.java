package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.BookingRequestCustomerDTO;
import com.fpt.capstone.tourism.model.AgeType;
import com.fpt.capstone.tourism.model.TourBookingCustomer;

import java.util.List;

public interface TourBookingCustomerMapper {
    List<TourBookingCustomer> toAdultEntity(List<BookingRequestCustomerDTO> adults);
    List<TourBookingCustomer> toChildrenEntity(List<BookingRequestCustomerDTO> children);
    TourBookingCustomer toEntity(BookingRequestCustomerDTO bookingRequestCustomerDTO, AgeType ageType);
}
