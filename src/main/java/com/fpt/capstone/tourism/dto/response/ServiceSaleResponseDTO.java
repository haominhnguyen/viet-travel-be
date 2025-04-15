package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.TourDayService;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class ServiceSaleResponseDTO {
    private Long id;
    private String name;
    private double nettPrice;
    private Boolean deleted;
    private ServiceCategoryDTO serviceCategory;
    private ServiceProviderSaleResponseDTO serviceProvider;
}
