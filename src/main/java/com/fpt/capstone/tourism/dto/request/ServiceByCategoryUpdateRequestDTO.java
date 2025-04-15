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
public class ServiceByCategoryUpdateRequestDTO {
    private Integer dayNumber;
    private Long locationId;
    private Long serviceProviderId;
    private Long serviceId;
    private Double nettPrice;
    private Double sellingPrice;
    private Map<Long, Double> paxPrices; // Key: paxId, Value: price

    // Specific attributes for different service types
    private RoomUpdateDTO roomDetail;
    private MealUpdateDTO mealDetail;
    private TransportUpdateDTO transportDetail;
}
