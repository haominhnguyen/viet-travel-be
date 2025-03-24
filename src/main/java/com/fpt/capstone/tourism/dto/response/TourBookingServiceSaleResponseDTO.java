package com.fpt.capstone.tourism.dto.response;


import com.fpt.capstone.tourism.dto.common.TourDayDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourBookingServiceSaleResponseDTO {
    private TourDayDTO tourDay;
    private List<TourBookingServiceDTO> bookingServices;
}
