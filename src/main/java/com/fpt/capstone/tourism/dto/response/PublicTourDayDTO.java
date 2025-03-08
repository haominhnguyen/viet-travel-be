package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourDayService;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class PublicTourDayDTO {
    private Long id;
    private String title;
    private String content;
    private String mealPlan;
}
