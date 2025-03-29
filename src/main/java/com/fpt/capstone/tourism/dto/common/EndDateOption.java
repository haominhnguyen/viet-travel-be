package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EndDateOption {
    private LocalDateTime endDate;
    private String description;
    private boolean isDefault;
}
