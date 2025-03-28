package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourDayServiceCategoryDTO {
    private Long id;
    private ServiceCategoryDTO serviceCategory;
}
