package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.TourDayServiceDTO;
import com.fpt.capstone.tourism.model.TourDayServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleTourDayResponseDTO {
    private Long id;
    private String title;
    private Integer dayNumber;
    private String content;
    private String mealPlan;
    private List<TourDayServiceDTO> tourDayServiceCategories;
}
