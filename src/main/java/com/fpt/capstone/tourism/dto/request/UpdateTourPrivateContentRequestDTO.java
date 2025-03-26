package com.fpt.capstone.tourism.dto.request;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UpdateTourPrivateContentRequestDTO {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String highlights;
    private String privacy;
    private String notes;
    private List<TourDayPrivateRequestDTO> tourDays;
}
