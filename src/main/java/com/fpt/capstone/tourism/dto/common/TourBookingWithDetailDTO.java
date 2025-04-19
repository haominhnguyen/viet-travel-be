package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.dto.response.TourBookingShortSaleResponseDTO;
import com.fpt.capstone.tourism.model.TourBookingCustomer;
import com.fpt.capstone.tourism.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourBookingWithDetailDTO {
    private TourBookingShortSaleResponseDTO tourBooking;
    private BookedPersonDTO bookedCustomer;
}
