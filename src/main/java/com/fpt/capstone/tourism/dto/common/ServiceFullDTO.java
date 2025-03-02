package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceFullDTO  {
    private Long id;
    private String name;
    private double nettPrice;
    private double sellingPrice;
    private String imageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean deleted;
    private ServiceCategoryDTO serviceCategory;
    private ServiceProviderDTO serviceProvider;
    private Set<ServiceDetailDTO> serviceDetails;
    private List<TourDayServiceDTO> tourDayServices;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

