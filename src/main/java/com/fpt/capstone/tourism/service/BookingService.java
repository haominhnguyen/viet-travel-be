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
     GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>> getTourBookings(int page, int size, String keyword, String status, String sortField, String sortDirection);

     GeneralResponse<PagingDTO<List<TourWithNumberBookingDTO>>> getTours(int page, int size, String keyword, TourStatus status, String sortField, String sortDirection, TourType tourType);

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

     GeneralResponse<?> sendCheckingServiceAvailable(CheckingServiceAvailableDTO dto);

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

     GeneralResponse<?> cancelBooking(CancelTourBookingRequestDTO dto);

     GeneralResponse<?> sendPricing(Long tourId);

     GeneralResponse<?> updateBookingStatus(BookingStatusUpdateDTO dto);

     GeneralResponse<?> sendOperator(SendOperatorDTO dto);

     GeneralResponse<?> takeBooking(TakeBookingRequestDTO dto);

     GeneralResponse<?> changePaymentMethod(Long id, PaymentMethod paymentMethod);

    GeneralResponse<?> cancelBooking(String bookingCode);

     GeneralResponse<?> createCustomer(SaleCreateUserRequestDTO dto);

     GeneralResponse<?> getForwardSchedule(ForwardScheduleRequestDTO dto);

     GeneralResponse<?> forwardBooking(ForwardBookingRequestDTO dto);

     GeneralResponse<?> checkingAllService(Long bookingId);

     GeneralResponse<?> getEmailContent(SendPriceRequestDTO dto);

     GeneralResponse<?> sendEmailPrice(SendEmailPriceRequestDTO dto);

     void confirmPayment(int paymentStatus, String orderInfo);

     GeneralResponse<?> getAllRefundRequest(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection);

     GeneralResponse<?> getDetailRefundRequest(Long tourBookingId);

    GeneralResponse<?> approveRefundRequest(Long tourBookingId);

    GeneralResponse<?> rejectRefundRequest(Long tourBookingId);

    GeneralResponse<?> successService(Long tourBookingServiceId);
}
