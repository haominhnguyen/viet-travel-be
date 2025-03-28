package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TourBookingServiceCommonDTO {
    private Long id;
    private TourBooking booking;
    private Service service;
    private TourDay tourDay;
    private int currentQuantity;
    private int requestedQuantity;
    private LocalDateTime requestDate;
    private String reason;
    private TourBookingServiceStatus status;
}
