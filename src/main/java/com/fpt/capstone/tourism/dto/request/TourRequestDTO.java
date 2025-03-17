package com.fpt.capstone.tourism.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourRequestDTO {
    private String name;
    private String highlights;
    private Integer numberDays;
    private Integer numberNights;
    private String note;
    private List<Long> locationIds;
    private List<Long> tagIds;
    private String tourType;
    private String tourStatus;
    private Long departLocationId;
    private Double markUpPercent;
    private String privacy;
    private List<TourImageRequestDTO> tourImages;
}
