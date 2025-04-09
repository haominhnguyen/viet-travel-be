package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportDTO {
    private Long id;
    private Integer seatCapacity;
    private Boolean deleted;
    private Long serviceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long categoryId;
    private String categoryName;
}
