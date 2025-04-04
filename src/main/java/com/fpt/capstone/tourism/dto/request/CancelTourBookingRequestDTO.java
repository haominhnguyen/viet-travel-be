package com.fpt.capstone.tourism.dto.request;


import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelTourBookingRequestDTO {
    private Long bookingId;
    private TourBookingStatus status;
    private String reason;
}
