package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.dto.common.TourDayDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceWithServiceOnlyDTO;
import com.fpt.capstone.tourism.dto.common.TourDayShortInfoDTO;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.TourDayServiceCategory;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@Builder
public class ServiceCategoryWithTourDayResponseDTO {
    private Long id;
    private String categoryName;
    private List<TourDayShortInfoDTO> tourDays;
}
