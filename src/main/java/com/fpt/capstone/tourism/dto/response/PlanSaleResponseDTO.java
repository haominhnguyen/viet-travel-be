package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.enums.PlanStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanSaleResponseDTO {
    private Long id;
    private String content;
    private PlanStatus planStatus;
    private UserBasicDTO user;
}
