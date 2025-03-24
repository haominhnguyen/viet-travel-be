package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderServicesDetailDTO {
    private Long providerId;
    private String providerName;
    private String providerImageUrl;
    private Integer providerStar;
    private String providerPhone;
    private String providerEmail;
    private String providerAddress;
    private Long locationId;
    private String locationName;
    private List<AvailableServiceDTO> availableServices;
}
