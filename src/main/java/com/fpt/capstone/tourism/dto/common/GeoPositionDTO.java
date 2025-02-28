package com.fpt.capstone.tourism.dto.common;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

import java.util.Objects;


@Data
public class GeoPositionDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
}
