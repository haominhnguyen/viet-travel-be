package com.fpt.capstone.tourism.dto.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderDetailDTO {
    private Long id;
    private String imageUrl;
    private String name;
    private String abbreviation;
    private String website;
    private String email;
    private int star;
    private String phone;
    private String address;
    private Boolean deleted;
    private PublicLocationSimpleDTO location;
    private GeoPositionDTO geoPosition;
    private List<ServiceCategoryDTO> serviceCategories;
}
