package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourDayServiceDTO{
    private Long id;
    private Integer quantity;
    private Double sellingPrice;
    private TourDayDTO tourDay;
}

