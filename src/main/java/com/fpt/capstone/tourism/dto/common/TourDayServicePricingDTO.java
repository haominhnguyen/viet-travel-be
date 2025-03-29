package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDayServicePricingDTO {
    private Long tourDayServiceId;
    private Long serviceId;
    private String serviceName;
    private Integer dayNumber;
    private String categoryName;
    private Double defaultServicePrice;
    private Double customServicePrice;
    private boolean isAssociated;
}
