package com.fpt.capstone.tourism.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourScheduleRequestDTO {
    private Long scheduleId;
    private Long tourId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long operatorId;
    private Long tourPaxId;
}
