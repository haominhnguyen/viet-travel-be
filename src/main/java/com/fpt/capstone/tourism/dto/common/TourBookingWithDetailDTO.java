package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.TourBookingCustomer;
import com.fpt.capstone.tourism.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourBookingWithDetailDTO {
    private TourBookingDTO tourBooking;
    private Double total;
    private Double paid;
    private Double remaining;
    private StaffDTO operator;
    private StaffDTO salesman;
    private BookedPersonDTO bookedCustomer;
}
