package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.enums.MealType;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealDetailsDTO {
    private MealType type;
    private String mealDetail;
}
