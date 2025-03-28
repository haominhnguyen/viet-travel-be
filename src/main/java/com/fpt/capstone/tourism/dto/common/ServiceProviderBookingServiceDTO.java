package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Builder
public class ServiceProviderBookingServiceDTO {
    private Long id;
    private String serviceName;
    private Integer currentQuantity;
    private Integer requestedQuantity;
    private LocalDateTime requestDate;
    private String reason;
    private TourBookingServiceStatus status;
}
