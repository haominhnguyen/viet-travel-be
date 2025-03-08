package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.BookingRequestDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.TourBookingDataResponseDTO;

public interface BookingService {
     GeneralResponse<TourBookingDataResponseDTO> viewTourBookingDetail(Long tourId, Long scheduleId);
     GeneralResponse<?> createBooking(BookingRequestDTO bookingRequestDTO);
     GeneralResponse<?> getTourBookingDetails(String bookingCode);
     GeneralResponse<?> viewListBooking();
}
