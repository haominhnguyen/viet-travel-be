package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourDayShortInfoDTO {
    private Long id;
    private String title;
    private Integer dayNumber;
    private List<TourDayServiceWithServiceOnlyDTO> tourDayServices;
}
