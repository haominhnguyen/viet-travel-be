package com.fpt.capstone.tourism.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForwardBookingRequestDTO {
    private Long bookingId;
    private Long scheduleId;
}
