package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderOptionsDTO {
    private List<ServiceProviderOptionDTO> serviceProviders;
    private Long locationId;
    private String locationName;
    private String categoryName;
}
