package com.fpt.capstone.tourism.dto.common;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendOperatorDTO {
    private Long tourId;
    private Long tourScheduleId;
}
