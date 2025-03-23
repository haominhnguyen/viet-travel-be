package com.fpt.capstone.tourism.dto.request;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateServiceNotBookingSaleRequestDTO {
    private Long tourBookingServiceId;
    private int currentQuantity;
}
