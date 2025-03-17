package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDayServiceUpdateRequestDTO {
    private Long id;
    private Long serviceId;
    private Integer quantity;
    private Double sellingPrice;
}
