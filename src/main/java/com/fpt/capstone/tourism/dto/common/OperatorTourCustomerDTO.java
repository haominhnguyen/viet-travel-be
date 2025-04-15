package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OperatorTourCustomerDTO {
    private Long tourBookingId;
    private String tourBookingCode;
    private TourBookingCategory tourBookingCategory;
    private List<TourBookingCustomerDTO> listCustomer;
}
