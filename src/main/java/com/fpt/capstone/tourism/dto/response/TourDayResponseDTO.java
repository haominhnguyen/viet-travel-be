package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceFullDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TourDayResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String mealPlan;
    private PublicLocationDTO location;
    private List<TourDayServiceFullDTO> tourDayServices;
}
