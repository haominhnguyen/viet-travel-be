package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourDayService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDayDTO {
    private Long id;
    private String title;
    private Integer dayNumber;
    private String mealPlan;
    private Boolean deleted;
    private LocationDTO location;
}
