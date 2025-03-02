package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import com.fpt.capstone.tourism.model.ActivityCategory;
import com.fpt.capstone.tourism.model.GeoPosition;
import com.fpt.capstone.tourism.model.Location;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class ActivityDTO {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private double pricePerPerson;
    private boolean deleted;
    private GeoPositionDTO geoPosition;
    private PublicLocationDTO location;
    private ActivityCategoryDTO activityCategory;
}
