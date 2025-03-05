package com.fpt.capstone.tourism.dto.response;

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
}

