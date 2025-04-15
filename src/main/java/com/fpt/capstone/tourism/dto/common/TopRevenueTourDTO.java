package com.fpt.capstone.tourism.dto.common;
import com.fpt.capstone.tourism.model.enums.TourType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopRevenueTourDTO {
    private Long tourId;
    private String tourName;
    private TourType tourType;
    private BigDecimal totalRevenue;
}

