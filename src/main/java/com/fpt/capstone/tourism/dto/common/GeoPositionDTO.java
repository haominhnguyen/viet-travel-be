package com.fpt.capstone.tourism.dto.common;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoPositionDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
}
