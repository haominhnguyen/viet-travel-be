package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.ActivityCategoryDTO;
import com.fpt.capstone.tourism.dto.common.GeoPositionDTO;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PublicActivityDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private double sellingPrice;
    private PublicLocationDTO location;
}
