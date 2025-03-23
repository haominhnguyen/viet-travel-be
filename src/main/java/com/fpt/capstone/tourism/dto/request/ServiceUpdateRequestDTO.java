package com.fpt.capstone.tourism.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdateRequestDTO {
    private Long serviceId;
    private Integer dayNumber;
    private Long locationId;
    private Long serviceProviderId;
    private Map<Long, Double> paxPrices; // Key: paxId, Value: price
}
