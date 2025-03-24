package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableServiceDTO {
    private Long id;
    private String name;
    private String categoryName;
    private Double nettPrice;
    private Double sellingPrice;
    private String status; // ACTIVE, EXPIRED, UPCOMING
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private RoomDetailDTO roomDetail;
    private MealDetailDTO mealDetail;
    private TransportDetailDTO transportDetail;
}
