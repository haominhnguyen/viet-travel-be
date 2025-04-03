package com.fpt.capstone.tourism.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourPrivateServiceRequestDTO {
    private Long id;
    private String title;
    private int dayNumber;
    private List<Long> services;
}
