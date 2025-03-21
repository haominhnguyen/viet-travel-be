package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.CreatePublicBookingRequestDTO;
import com.fpt.capstone.tourism.dto.request.UpdateCustomersRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingDataResponseDTO;
import com.fpt.capstone.tourism.model.enums.TourType;

import java.util.List;

public interface BookingService {
     GeneralResponse<TourBookingDataResponseDTO> viewTourBookingDetail(Long tourId, Long scheduleId);
     GeneralResponse<?> createBooking(BookingRequestDTO bookingRequestDTO);
     GeneralResponse<?> getTourBookingDetails(String bookingCode);
     GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>> getTourBookings(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection);

     GeneralResponse<PagingDTO<List<TourWithNumberBookingDTO>>> getTours(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection, TourType tourType);

     GeneralResponse<?> createBooking(CreatePublicBookingRequestDTO bookingRequestDTO);

     GeneralResponse<?> getTourListBookings(Long tourId, Long scheduleId);


     GeneralResponse<?> saleViewBookingDetails(Long bookingId);

     GeneralResponse<?> getTourBookingCustomers(Long bookingId);

     GeneralResponse<?> changeCustomerStatus(Long customerId);


     GeneralResponse<?> updateCustomers(UpdateCustomersRequestDTO updateCustomersRequestDTO);


     GeneralResponse<?> getTourDetails(Long tourId);

     GeneralResponse<?> getTourDetails(Long tourId, Long scheduleId);

     GeneralResponse<?> getCustomersByName(String name);
}
