package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.dto.common.MealDetailDTO;
import com.fpt.capstone.tourism.dto.common.RoomDetailDTO;
import com.fpt.capstone.tourism.dto.common.TransportDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCreateRequestDTO {
        private Long serviceId;
        private Long locationId;
        private Long serviceProviderId;
        private Integer dayNumber;
        private Integer quantity;
        private Double sellingPrice;
        private Double nettPrice;
        private Map<String, Double> paxPrices;

        // Service-specific details
        private RoomDetailDTO roomDetail;
        private MealDetailDTO mealDetail;
        private TransportDetailDTO transportDetail;
}
