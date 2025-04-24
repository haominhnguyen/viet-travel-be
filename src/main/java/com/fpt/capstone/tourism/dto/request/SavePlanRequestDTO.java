package com.fpt.capstone.tourism.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavePlanRequestDTO {
    private Long userId;
    private String plan;
}
