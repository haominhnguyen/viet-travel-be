package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourBookingCustomer;
import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourBookingDTO {
    private Long id;
    private int seats;
    private String note;
    private String bookingCode;
    private Boolean deleted;
    private TourShortInfoDTO tour;
    private TourScheduleShortInfoDTO tourSchedule;
    private TourBookingStatus status;
    private TourBookingCategory tourBookingCategory;
    private Double sellingPrice;
    private Double extraHotelCost;
    private PaymentMethod paymentMethod;
    private String reason;
}
