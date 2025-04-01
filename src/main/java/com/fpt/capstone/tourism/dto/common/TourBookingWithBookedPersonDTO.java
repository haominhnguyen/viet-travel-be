package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourBookingWithBookedPersonDTO {
    private Long id;
    private String bookingCode;
    private BookedPersonDTO bookedPerson;
}
