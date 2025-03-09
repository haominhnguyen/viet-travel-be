package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourBookingWithDetailDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.TourBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface BookingHelper {

    String generateBookingCode(Long tourId, Long scheduleId, Long customerId);
    GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>> buildPagedResponse(Page<TourBooking> tourBookingPage);
    Specification<TourBooking> buildSearchSpecification(String keyword, Boolean isDeleted);
}
