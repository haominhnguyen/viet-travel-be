package com.fpt.capstone.tourism.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendPriceRequestDTO {
    private Long tourId;
    private Long scheduleId;
}
