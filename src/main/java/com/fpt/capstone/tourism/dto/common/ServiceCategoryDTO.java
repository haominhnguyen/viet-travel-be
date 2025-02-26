package com.fpt.capstone.tourism.dto.common;

import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryDTO {
    private Long id;
    private String name;
    private Boolean deleted;
}
