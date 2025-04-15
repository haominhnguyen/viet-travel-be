package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDayFullDTO {
    private Long id;
    private String title;
    private Integer dayNumber;
    private String content;
    private String mealPlan;
    private Long tourId;
    private LocationDTO location;
    private Boolean deleted;
    private List<String> serviceCategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
