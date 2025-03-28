package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourMarkupUpdateRequestDTO {
    private Double markUpPercent;
}
