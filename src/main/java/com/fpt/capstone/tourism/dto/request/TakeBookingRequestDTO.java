package com.fpt.capstone.tourism.dto.request;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TakeBookingRequestDTO {
    private Long bookingId;
    private Long saleId;
}
