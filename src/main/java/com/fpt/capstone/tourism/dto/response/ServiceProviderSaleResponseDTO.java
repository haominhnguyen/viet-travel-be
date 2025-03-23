package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.GeoPosition;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServiceProviderSaleResponseDTO {
    private Long id;
    private String name;
    private String abbreviation;
    private Boolean deleted;
}
