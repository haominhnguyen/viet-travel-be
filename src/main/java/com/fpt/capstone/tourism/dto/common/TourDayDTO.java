package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourDayService;
import jakarta.persistence.*;

import java.util.Set;

public class TourDayDTO {
    private Long id;
    private String title;
    private String content;

    private String mealPlan;

    private Boolean deleted;

    private Tour tour;

    private Location location;

    private Set<TourDayService> tourDayServices;
}
