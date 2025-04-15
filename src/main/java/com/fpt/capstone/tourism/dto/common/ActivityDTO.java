package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
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
