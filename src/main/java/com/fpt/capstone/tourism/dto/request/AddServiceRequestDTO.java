package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.Service;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AddServiceRequestDTO {
    private Long bookingId;
    private Long serviceId;
    private Integer addQuantity;
    private LocalDateTime requestDate;
    private String reason;
}
