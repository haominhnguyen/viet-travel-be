package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSummaryDTO {
    private Long id;
    private String name;
    private Integer dayNumber;
    private String status; // ACTIVE, EXPIRED, UPCOMING
    private Double nettPrice;
    private Double sellingPrice;
    private String locationName;
    private Long locationId;
    private String serviceProviderName;
    private Long serviceProviderId;
    private Map<String, PaxPriceInfoDTO> paxPrices; // Key: "1-2", "3-5", etc. Value: price
}
