package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.ServiceBasicDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDayServiceResponseDTO {
    private Long id;
    private Long tourDayId;
    private ServiceBasicDTO service;
    private Integer quantity;
    private Double sellingPrice;
}
