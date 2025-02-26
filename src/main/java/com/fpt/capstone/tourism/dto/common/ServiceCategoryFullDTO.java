package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryFullDTO {
    private Long id;
    private String categoryName;
    private Boolean deleted;
    private List<ServiceProviderSimpleDTO> serviceProviders;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

