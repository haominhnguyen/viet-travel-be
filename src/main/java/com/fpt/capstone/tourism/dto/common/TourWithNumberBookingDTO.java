package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourWithNumberBookingDTO {
    private TourDTO tour;
    private long numberBooking;
}
