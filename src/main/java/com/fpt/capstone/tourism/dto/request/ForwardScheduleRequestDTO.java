package com.fpt.capstone.tourism.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForwardScheduleRequestDTO {
    private Long tourId;
    private Long scheduleId;
    private int seats;
}
