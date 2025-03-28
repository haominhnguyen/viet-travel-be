package com.fpt.capstone.tourism.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourMarkupResponseDTO {
    private Long tourId;
    private String tourName;
    private Double markUpPercent;
}
