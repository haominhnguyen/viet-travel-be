package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.dto.common.TourBookingCustomerDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class UpdateCustomersRequestDTO {
    private List<TourBookingCustomerDTO> customers;
    private Long bookingId;
    // Getters & Setters
}