package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.model.TourBookingCustomer;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class BookingConfirmResponse {
    private Long id;
    private String bookingCode;
    private LocalDateTime createdAt;
    private BookedPersonDTO bookedPerson;
    private Double sellingPrice;
    private Double extraHotelCost;
    private PaymentMethod paymentMethod;
    private TourShortInfoDTO tour;
    private String note;
    private TourScheduleShortInfoDTO tourSchedule;
    private List<TourCustomerDTO> adults;
    private List<TourCustomerDTO> children;
    private String paymentUrl;
    private TourBookingStatus status;
}
