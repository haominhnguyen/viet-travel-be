package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePricingRequestDTO {
    private Long tourDayServiceId;
    private Double customServicePrice;
}
