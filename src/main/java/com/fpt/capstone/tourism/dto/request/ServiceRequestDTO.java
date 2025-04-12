package com.fpt.capstone.tourism.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestDTO {
    private String name;
    private Double nettPrice;
    private Double sellingPrice;
    private String imageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long categoryId;
    private Long providerId;
    private RoomDetailsDTO roomDetails;
    private MealDetailsDTO mealDetails;
    private TransportDetailsDTO transportDetails;
}
