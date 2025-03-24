package com.fpt.capstone.tourism.dto.common;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealDetailDTO {
    private Long id;
    private String type; // Breakfast, Lunch, Dinner
    private String mealDetail;
}