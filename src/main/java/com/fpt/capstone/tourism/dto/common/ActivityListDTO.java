package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityListDTO {
    private Long id;
    private Integer dayNumber;
    private String locationName;
    private String activityName;
    private String categoryName;
    private Double pricePerPerson;
}