package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealUpdateDTO {
    private String type; // Breakfast, Lunch, Dinner
    private String mealDetail;
}