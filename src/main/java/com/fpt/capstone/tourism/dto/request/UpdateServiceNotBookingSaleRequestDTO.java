package com.fpt.capstone.tourism.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceNotBookingSaleRequestDTO {
    private Long tourBookingServiceId;
    private int currentQuantity;
}
