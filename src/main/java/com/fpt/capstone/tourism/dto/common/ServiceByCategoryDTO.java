package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceByCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Integer dayNumber;
    private String status; // ACTIVE, EXPIRED, UPCOMING
    private Double nettPrice;
    private Double sellingPrice;
    private Long locationId;
    private String locationName;
    private Long serviceProviderId;
    private String serviceProviderName;
    private String categoryName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Map<String, PaxPriceInfoDTO> paxPrices; // Key: paxId, Value: PaxPriceInfoDTO
    private RoomDetailDTO roomDetail;
    private MealDetailDTO mealDetail;
    private TransportDetailDTO transportDetail;
}
