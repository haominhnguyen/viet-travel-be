package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@AllArgsConstructor
public class OperatorAvailabilityDTO {
    private Long id;
    private String fullName;
    private int activeToursCount;
}
