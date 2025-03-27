package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderServicesDTO {
    private Long categoryId;
    private String categoryName;
    private Long providerId;
    private String providerName;
    private Long locationId;
    private String locationName;
    private List<AvailableServiceDTO> availableServices;
}
