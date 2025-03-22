package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.enums.MealType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MealSimpleDTO {
    private Long id;
    private String type;  // Loại bữa ăn (Sáng, Trưa, Tối)
    private String mealDetail;
}
