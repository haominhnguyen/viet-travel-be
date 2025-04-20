package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.dto.common.TourBookingCustomerDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomersRequestDTO {
    private List<TourBookingCustomerDTO> customers;
    private Long bookingId;
    // Getters & Setters
}