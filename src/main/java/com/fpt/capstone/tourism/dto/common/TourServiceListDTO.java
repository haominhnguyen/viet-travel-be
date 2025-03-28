package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourServiceListDTO {
    private Long tourId;
    private String tourName;
    private List<TourServiceCategoryDTO> serviceCategories;
    private List<TourPaxOptionDTO> paxOptions;
}
