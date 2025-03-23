package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourPaxOptionDTO {
    private Long id;
    private int minPax;
    private int maxPax;
    private String paxRange;
}