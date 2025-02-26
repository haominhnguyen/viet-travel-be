package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.GeoPositionDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublicLocationDTO {
    private Long id;
    private String name;
    private String description;
    private String image;
}
