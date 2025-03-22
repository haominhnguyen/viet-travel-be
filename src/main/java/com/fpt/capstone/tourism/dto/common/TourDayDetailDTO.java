package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.TourDay;
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
public class TourDayDetailDTO {
    private Long id;
    private Integer dayNumber;
    private String title;
    private String content;
    private String mealPlan;
    private Boolean deleted;
    private Long tourId;
    private Long locationId;
    private String locationName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TourDayServiceDTO> services;
    private List<ServiceCategoryDTO> availableServiceCategories;
    private List<ServiceProviderDTO> availableServiceProviders;

    public static TourDayDetailDTO fromEntity(TourDay tourDay) {
        return TourDayDetailDTO.builder()
                .id(tourDay.getId())
                .dayNumber(tourDay.getDayNumber())
                .title(tourDay.getTitle())
                .content(tourDay.getContent())
                .mealPlan(tourDay.getMealPlan())
                .deleted(tourDay.getDeleted())
                .tourId(tourDay.getTour().getId())
                .locationId(tourDay.getLocation() != null ? tourDay.getLocation().getId() : null)
                .locationName(tourDay.getLocation() != null ? tourDay.getLocation().getName() : null)
                .createdAt(tourDay.getCreatedAt())
                .updatedAt(tourDay.getUpdatedAt())
                .build();
    }
}
