package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.TourDayServiceResponseDTO;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourDayService;
import com.fpt.capstone.tourism.model.TourDayServiceCategory;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Builder
@Data
public class TourDayProcessDetailDTO {
    private Long id;
    private Integer dayNumber;
    private String title;
    private String content;
    private String mealPlan;
    private Boolean deleted;
    private PublicLocationSimpleDTO location;
    private List<TourDayServiceProcessDTO> tourDayServices;
}
