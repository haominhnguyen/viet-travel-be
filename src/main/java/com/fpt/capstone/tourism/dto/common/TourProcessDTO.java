package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class TourProcessDTO {
    private Long id;
    private String name;
    private TourType tourType;
    private LocalDateTime createdAt;
    private TourStatus tourStatus;
}
