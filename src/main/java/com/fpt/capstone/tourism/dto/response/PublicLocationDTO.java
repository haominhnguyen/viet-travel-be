package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.GeoPositionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicLocationDTO {
    private Long id;
    private String name;
    private String description;
    private String image;
    private GeoPositionDTO geoPosition;
}
