package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.TourSchedule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourOperationLogDTO {
    private Long id;
    private String content;
    private String action;
    private Boolean deleted;
    private LocalDateTime createdAt;
}
