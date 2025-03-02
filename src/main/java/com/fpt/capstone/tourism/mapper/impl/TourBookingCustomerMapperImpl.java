package com.fpt.capstone.tourism.mapper.impl;

import com.fpt.capstone.tourism.dto.common.BookingRequestCustomerDTO;
import com.fpt.capstone.tourism.mapper.TourBookingCustomerMapper;
import com.fpt.capstone.tourism.model.AgeType;
import com.fpt.capstone.tourism.model.TourBookingCustomer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TourBookingCustomerMapperImpl implements TourBookingCustomerMapper {

    @Override
    public List<TourBookingCustomer> toAdultEntity(List<BookingRequestCustomerDTO> adults) {
        return adults.stream().map(bookingRequestCustomerDTO -> toEntity(bookingRequestCustomerDTO, AgeType.ADULT)).collect(Collectors.toList());
    }



    @Override
    public List<TourBookingCustomer> toChildrenEntity(List<BookingRequestCustomerDTO> children) {
        return children.stream().map(bookingRequestCustomerDTO -> toEntity(bookingRequestCustomerDTO, AgeType.CHILDREN)).collect(Collectors.toList());
    }

    @Override
    public TourBookingCustomer toEntity(BookingRequestCustomerDTO bookingRequestCustomerDTO, AgeType ageType) {

        return TourBookingCustomer.builder()
                .ageType(ageType)
                .customerName(bookingRequestCustomerDTO.getFullName())
                .gender(bookingRequestCustomerDTO.getGender())
                .singleRoom(bookingRequestCustomerDTO.isSingleRoom())
                .dateOfBirth(bookingRequestCustomerDTO.getDateOfBirth())
                .build();
    }


}
