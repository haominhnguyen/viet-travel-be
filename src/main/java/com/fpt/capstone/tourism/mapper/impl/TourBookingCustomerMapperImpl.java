package com.fpt.capstone.tourism.mapper.impl;

import com.fpt.capstone.tourism.dto.common.BookedPersonDTO;
import com.fpt.capstone.tourism.dto.common.BookingRequestCustomerDTO;
import com.fpt.capstone.tourism.dto.common.TourCustomerDTO;
import com.fpt.capstone.tourism.mapper.TourBookingCustomerMapper;
import com.fpt.capstone.tourism.model.enums.AgeType;
import com.fpt.capstone.tourism.model.TourBookingCustomer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TourBookingCustomerMapperImpl implements TourBookingCustomerMapper {

    @Override
    public List<TourBookingCustomer> toAdultEntity(List<BookingRequestCustomerDTO> adults) {
        return adults.stream().map(bookingRequestCustomerDTO -> toEntity(bookingRequestCustomerDTO, AgeType.ADULT, false)).collect(Collectors.toList());
    }



    @Override
    public List<TourBookingCustomer> toChildrenEntity(List<BookingRequestCustomerDTO> children) {
        return children.stream().map(bookingRequestCustomerDTO -> toEntity(bookingRequestCustomerDTO, AgeType.CHILDREN, false)).collect(Collectors.toList());
    }

    @Override
    public TourBookingCustomer toEntity(BookingRequestCustomerDTO bookingRequestCustomerDTO, AgeType ageType, boolean bookedPerson) {

        return TourBookingCustomer.builder()
                .ageType(ageType)
                .fullName(bookingRequestCustomerDTO.getFullName())
                .gender(bookingRequestCustomerDTO.getGender())
                .singleRoom(bookingRequestCustomerDTO.isSingleRoom())
                .dateOfBirth(bookingRequestCustomerDTO.getDateOfBirth())
                .bookedPerson(bookedPerson)
                .deleted(false)
                .build();
    }

    @Override
    public TourCustomerDTO toTourCustomerDTO(TourBookingCustomer tourBookingCustomer) {
        return TourCustomerDTO.builder()
                .fullName(tourBookingCustomer.getFullName())
                .gender(tourBookingCustomer.getGender())
                .singleRoom(tourBookingCustomer.getSingleRoom())
                .dateOfBirth(tourBookingCustomer.getDateOfBirth())
                .build();
    }

    @Override
    public BookedPersonDTO toBookedPersonDTO(TourBookingCustomer tourBookingCustomer) {
        return BookedPersonDTO.builder()
                .fullName(tourBookingCustomer.getFullName())
                .phone(tourBookingCustomer.getPhoneNumber())
                .email(tourBookingCustomer.getEmail())
                .address(tourBookingCustomer.getAddress())
                .build();
    }


}
