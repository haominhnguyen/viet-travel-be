package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBasicDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private double nettPrice;
    private double sellingPrice;
    private ServiceCategoryDTO serviceCategory;
    private ServiceProviderSimpleDTO serviceProvider;
}
