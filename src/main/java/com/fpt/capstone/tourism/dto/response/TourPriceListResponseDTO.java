package com.fpt.capstone.tourism.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourPriceListResponseDTO {
    private Long tourId;
    private String tourName;
    private List<TourPriceConfigResponseDTO> priceConfigurations;
}
