package com.fpt.capstone.tourism.dto.request;


import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class GeneratePlanRequestDTO {
    private Long locationId;
    private Long userId;
    private String locationName;
    private Date startDate;
    private Date endDate;
    private String preferences;
    private String planType;
    private boolean travelingWithChildren;
}
