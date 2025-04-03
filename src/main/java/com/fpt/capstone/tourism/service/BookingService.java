package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingDataResponseDTO;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourStatus;
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

     GeneralResponse<?> updateTourBookingService(Long tourBookingServiceID);

     Transaction createReceiptBookingTransaction(TourBooking tourBooking, Double total, String fullName, PaymentMethod paymentMethod);

     void saveTourBookingService(TourBooking tourBooking);

     GeneralResponse<?> getTourBookingServices(Long tourBookingID);

     GeneralResponse<?> updateServiceQuantity(UpdateServiceNotBookingSaleRequestDTO updateServiceNotBookingSaleRequestDTO);


     GeneralResponse<?> cancelService(Long tourBookingServiceId);

     GeneralResponse<?> sendCheckingServiceAvailable(Long tourBookingServiceId);

     GeneralResponse<?> getTourPrivateByName(String name) ;

     GeneralResponse<?> getTourContents(Long tourId);

     GeneralResponse<?> getLocations();


     GeneralResponse<?> createTourPrivate(CreateTourPrivateRequestDTO tour);

     GeneralResponse<?> updateTourPrivate(UpdateTourPrivateContentRequestDTO tour);


     GeneralResponse<?> updateTourPrivateStatus(ChangeStatusTourPrivateRequestDTO tour);


     GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>> viewListBookingHistory(int page, int size, String keyword, String paymentStatus, String orderDate);


     GeneralResponse<?> getServiceCategoryWithTourDays(Long tourId);

     GeneralResponse<?> getTourLocations(Long tourId);

     GeneralResponse<?> getServiceProviders(Long locationId, String categoryName);

     GeneralResponse<?>  getServiceProviderServices(Long providerId, String categoryName);

     GeneralResponse<?> updateTourServices(List<TourPrivateServiceRequestDTO> dto);

     void updateTourDayServices(TourDay tourDay, List<Long> serviceIds);

     GeneralResponse<?> cancelTour(CancelTourBookingRequestDTO dto);

}
