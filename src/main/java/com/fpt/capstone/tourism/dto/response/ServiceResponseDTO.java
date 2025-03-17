package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.MealDTO;
import com.fpt.capstone.tourism.dto.common.RoomDTO;
import com.fpt.capstone.tourism.dto.common.TransportDTO;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponseDTO {
    private Long id;
    private String name;
    private Double nettPrice;
    private Double sellingPrice;
    private String imageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean deleted;
    private Long categoryId;
    private String categoryName;
    private Long providerId;
    private String providerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private RoomDTO roomDetails;
    private MealDTO mealDetails;
    private TransportDTO transportDetails;
}

