package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PublicLocationSimpleProviderDTO {
    private Long id;
    private String name;
    private Long geoPositionId;
    private Double latitude;
    private Double longitude;
}
