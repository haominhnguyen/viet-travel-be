package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.GeoPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private Long id;
    private String name;
    private String description;
    private String image;
    private boolean deleted;
    private GeoPositionDTO geoPosition;
    private LocalDateTime createdAt;
}
