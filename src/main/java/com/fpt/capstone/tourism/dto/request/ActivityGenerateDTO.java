package com.fpt.capstone.tourism.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityGenerateDTO {
    private String preferences;
    private String locationName;
    private int startIndex;
}
