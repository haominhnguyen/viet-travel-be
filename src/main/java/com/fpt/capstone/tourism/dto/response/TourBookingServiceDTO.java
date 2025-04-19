package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourBookingServiceDTO {
    private Long id;
    private ServiceSaleResponseDTO service;
    private int currentQuantity;
    private int requestedQuantity;
    private Boolean deleted;
    private LocalDateTime requestDate;
    private String reason;
    private TourBookingServiceStatus status; //(e.g., Pending, Approved, Rejected).
}
