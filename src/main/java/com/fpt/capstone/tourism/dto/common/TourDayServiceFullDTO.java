package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDayServiceFullDTO {
    private Long id;
    private Long serviceId;
    private String serviceName;
    private String serviceCategoryName;
    private Integer quantity;
    private Double sellingPrice;
}
