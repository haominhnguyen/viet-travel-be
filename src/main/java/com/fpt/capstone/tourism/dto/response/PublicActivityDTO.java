package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.ActivityCategoryDTO;
import com.fpt.capstone.tourism.dto.common.GeoPositionDTO;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PublicActivityDTO {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private double pricePerPerson;
    private GeoPositionDTO geoPosition;
    private PublicLocationDTO location;
    private ActivityCategoryDTO activityCategory;
}
