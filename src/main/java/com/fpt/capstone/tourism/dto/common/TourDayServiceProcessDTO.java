package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.TourDay;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourDayServiceProcessDTO {
    private Long id;
    private ServiceBasicDTO service;
    private Double sellingPrice;
}
