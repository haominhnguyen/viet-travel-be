package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.BookedPersonDTO;
import com.fpt.capstone.tourism.dto.common.TourScheduleShortInfoDTO;
import com.fpt.capstone.tourism.dto.common.TourShortInfoDTO;
import com.fpt.capstone.tourism.dto.common.TourSupportInfoDTO;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourBookingShortSaleResponseDTO {
    private Long id;
    private int seats;
    private String bookingCode;
    private TourSupportInfoDTO tour;
    private TourScheduleShortInfoDTO tourSchedule;
    private TourBookingStatus status;
    private PaymentMethod paymentMethod;
}
