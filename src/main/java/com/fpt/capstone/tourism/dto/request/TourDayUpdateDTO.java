package com.fpt.capstone.tourism.dto.request;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDayUpdateDTO {
    private String title;
    private String content;
    private String mealPlan;
    private Long locationId;
    private List<TourDayServiceRequestDTO> tourDayServices;
}
