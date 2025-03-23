package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourBookingDetailSaleResponseDTO;
import com.fpt.capstone.tourism.dto.common.TourBookingWithDetailDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceSaleResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingSaleResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingServiceSaleResponseDTO;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface BookingHelper {

    String generateBookingCode(Long tourId, Long scheduleId, Long customerId);
    GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>> buildPagedResponse(Page<TourBooking> tourBookingPage);
    Specification<TourBooking> buildSearchSpecification(String keyword, Boolean isDeleted);
    Double getPaidAmount(List<Transaction> tourBookingReceipts);
    Double getTotal(List<Transaction> tourBookingReceipts);
    List<TourBookingSaleResponseDTO>  setPaymentStatistics(List<TourBooking> tourBookings);
    TourBookingSaleResponseDTO setPaymentStatistic(TourBooking tourBooking);
    TourBookingDetailSaleResponseDTO setPaymentStatisticForBookingDetail(TourBooking tourBooking);
    List<TourBookingServiceSaleResponseDTO> getTourBookingListService(List<TourDay> tourDays, TourBooking tourBooking);
}
