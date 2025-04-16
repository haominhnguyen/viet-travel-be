package com.fpt.capstone.tourism.dto.common;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RefundDetailDTO {
    private Long id;
    private String name;
    private String bookingCode;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double amount;
    private String notes;
}
