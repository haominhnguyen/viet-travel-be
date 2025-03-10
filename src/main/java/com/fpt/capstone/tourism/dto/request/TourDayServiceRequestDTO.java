package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDayServiceRequestDTO {
    private Long id;
    private Long serviceId;
    private Integer quantity;
    private Double sellingPrice;
}
