package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TourDayServiceManageRequestDTO {
    private Long serviceId;
    private Integer quantity;
    private Double sellingPrice;
}
