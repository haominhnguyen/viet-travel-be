package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.enums.TourStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangeStatusTourPrivateRequestDTO {
    private Long id;
    private TourStatus tourStatus;
}
