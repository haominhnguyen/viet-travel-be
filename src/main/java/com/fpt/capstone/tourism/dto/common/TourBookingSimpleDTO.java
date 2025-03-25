package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourBookingSimpleDTO {
    private Long bookingId;
    private String bookingCode;
    private String customerName;
}
