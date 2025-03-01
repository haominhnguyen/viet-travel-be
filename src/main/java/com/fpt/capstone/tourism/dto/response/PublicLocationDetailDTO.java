package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.ActivityDTO;
import com.fpt.capstone.tourism.dto.common.GeoPositionDTO;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PublicLocationDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String image;
    private List<PublicTourDTO> tours;
    private List<BlogResponseDTO> blogs;
    private List<PublicActivityDTO> activities;
    private List<PublicLocationDTO> locations;
    private List<PublicServiceProviderDTO> hotels;
}
