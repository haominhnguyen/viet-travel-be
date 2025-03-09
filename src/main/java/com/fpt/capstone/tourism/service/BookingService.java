package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingDataResponseDTO;

import java.util.List;

public interface BookingService {
     GeneralResponse<TourBookingDataResponseDTO> viewTourBookingDetail(Long tourId, Long scheduleId);
     GeneralResponse<?> createBooking(BookingRequestDTO bookingRequestDTO);
     GeneralResponse<?> getTourBookingDetails(String bookingCode);
     GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>> getTourBookings(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection);

     GeneralResponse<PagingDTO<List<TourDTO>>> getPublicTours(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection);
}
