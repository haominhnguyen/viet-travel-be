package com.fpt.capstone.tourism.dto.common;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;


@Data
@Builder
public class GeoPositionDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
}
