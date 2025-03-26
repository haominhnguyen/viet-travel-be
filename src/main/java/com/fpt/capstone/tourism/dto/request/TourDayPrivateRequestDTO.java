package com.fpt.capstone.tourism.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourDayPrivateRequestDTO {
    private Long id;
    private String title;
    private String meals;
    private int dayNumber;
}
