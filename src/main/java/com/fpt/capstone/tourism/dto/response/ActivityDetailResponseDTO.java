package com.fpt.capstone.tourism.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDetailResponseDTO {
    private Long id;
    private String name;
    private double nettPrice;
    private double sellingPrice;
    private String imageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean deleted;

    // Service category fields
    private Long categoryId;
    private String categoryName;

    // Service provider fields
    private Long providerId;
    private String providerName;

    // Tour day related information
    private Integer dayNumber;
    private Long locationId;
    private String locationName;

    // Geo position information (if available)
    private Double latitude;
    private Double longitude;
}
