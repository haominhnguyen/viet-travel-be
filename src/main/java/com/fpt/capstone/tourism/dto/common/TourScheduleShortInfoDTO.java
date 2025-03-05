package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class TourScheduleShortInfoDTO {
    private Long scheduleId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double sellingPrice;
    private Double extraHotelCost;
}
