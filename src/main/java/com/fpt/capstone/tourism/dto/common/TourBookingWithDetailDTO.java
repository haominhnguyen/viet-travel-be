package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.dto.response.TourBookingShortSaleResponseDTO;
import com.fpt.capstone.tourism.model.TourBookingCustomer;
import com.fpt.capstone.tourism.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourBookingWithDetailDTO {
    private TourBookingShortSaleResponseDTO tourBooking;
    private BookedPersonDTO bookedCustomer;
}
