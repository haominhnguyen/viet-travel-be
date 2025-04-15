package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.common.GeoPositionDTO;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderDTO {
    private Long id;

    private String imageUrl;
    private String name;

    @NotBlank(message = "Abbreviation is required")
    private String abbreviation;

    private String website;

    @Email(message = "Invalid email format")
    private String email;
    private int star;

    private String phone;

    private String address;

    private Boolean deleted;

    private Long locationId;

    private GeoPositionDTO geoPosition;

    private List<ServiceCategoryDTO> serviceCategories;
}

