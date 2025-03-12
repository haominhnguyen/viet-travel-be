package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDayRequestDTO {
    private Long id;
    private String title;
    private String content;
    private String mealPlan;
    private Long tourId;
    private LocationDTO location;
    private List<TourDayServiceDTO> tourDayServices;
}
