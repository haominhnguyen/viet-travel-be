package com.fpt.capstone.tourism.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourOperationLogRequestDTO {
    private String content;
    private String action;
}
