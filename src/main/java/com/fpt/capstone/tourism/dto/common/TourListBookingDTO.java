package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.dto.response.TourBookingSaleResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourDetailSaleResponseDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourListBookingDTO {
    TourDetailSaleResponseDTO tour;
    List<TourBookingSaleResponseDTO> bookings;
}
