package com.fpt.capstone.tourism.dto.common;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private Long id;
    private String name;
    private double nettPrice;
    private double sellingPrice;
    private String imageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean deleted;
    private Long categoryId;
    private String categoryName;
    private Long providerId;
    private String providerName;
}

