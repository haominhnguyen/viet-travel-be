package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceForTourPrivateDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private double nettPrice;
    private double sellingPrice;
    private ServiceCategoryDTO serviceCategory;
    private ServiceProviderSimpleDTO serviceProvider;
}
