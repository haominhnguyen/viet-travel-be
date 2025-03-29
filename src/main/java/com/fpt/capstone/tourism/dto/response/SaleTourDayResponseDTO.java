package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.LocationShortDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceCategoryDTO;
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
    private List<TourDayServiceCategoryDTO> tourDayServiceCategories;
    private LocationShortDTO location;
}
