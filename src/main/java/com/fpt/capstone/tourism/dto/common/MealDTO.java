package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealDTO {
    private Long id;
    private MealType type;
    private Long serviceId;
    private Boolean deleted;
    private String mealDetail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long categoryId;
    private String categoryName;
}
