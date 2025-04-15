package com.fpt.capstone.tourism.dto.common;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBaseDTO {
    private Long id;
    private String name;
    private double nettPrice;
    private double sellingPrice;
    private String imageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean deleted;
    private Long serviceCategoryId;
    private String serviceCategoryName;
    private Long serviceProviderId;
    private String serviceProviderName;
    private String serviceProviderAbbreviation;
    private String serviceProviderImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


