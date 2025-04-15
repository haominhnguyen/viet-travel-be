package com.fpt.capstone.tourism.dto.response;


import com.fpt.capstone.tourism.model.enums.TourType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SaleTourBookingServiceListResponseDTO {
    List<TourBookingServiceSaleResponseDTO> servicesByDay;
    TourType tourType;
}
