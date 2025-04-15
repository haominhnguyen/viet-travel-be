package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourSupportInfoDTO {
    private Long id;
    private String name;
    private int numberDays;
    private int numberNights;
    private TourType tourType;
    private TourStatus tourStatus;
    private double markUpPercent;
}
