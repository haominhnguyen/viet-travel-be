package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class TourScheduleShortInfoDTO {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
