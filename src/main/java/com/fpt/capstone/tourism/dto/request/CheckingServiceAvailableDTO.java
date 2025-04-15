package com.fpt.capstone.tourism.dto.request;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckingServiceAvailableDTO {
    private Long tourBookingServiceId;
    private int newQuantity;
    private String reason;
}
