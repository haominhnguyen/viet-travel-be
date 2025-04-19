package com.fpt.capstone.tourism.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakeBookingRequestDTO {
    private Long bookingId;
    private Long saleId;
}
