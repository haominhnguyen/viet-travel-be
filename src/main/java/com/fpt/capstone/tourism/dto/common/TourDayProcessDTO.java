package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.TourDayServiceCategory;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourDayProcessDTO {
    private Long id;
    private Integer dayNumber;
    private String title;
    private String content;
    private Boolean deleted;
    private List<TourDayServiceCategoryDTO> tourDayServiceCategories;
}
