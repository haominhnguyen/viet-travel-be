package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.TourDayDTO;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.TourDay;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourDayServiceCategorySaleResponseDTO {
    private Long id;
    private TourDayDTO tourDay;
}
