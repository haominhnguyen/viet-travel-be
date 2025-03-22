package com.fpt.capstone.tourism.dto.response;


import com.fpt.capstone.tourism.dto.common.ServiceDTO;
import com.fpt.capstone.tourism.dto.common.TourDayDTO;
import com.fpt.capstone.tourism.model.TourDay;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourBookingServiceSaleResponseDTO {
    private TourDayDTO tourDayDTO;
    private List<ServiceDTO> serviceDTO;
}
