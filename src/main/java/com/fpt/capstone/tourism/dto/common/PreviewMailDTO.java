package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PreviewMailDTO {
    private Long bookingServiceId;
    private Long serviceId;
    private Integer orderQuantity;
    private LocalDateTime requestDate;
}
