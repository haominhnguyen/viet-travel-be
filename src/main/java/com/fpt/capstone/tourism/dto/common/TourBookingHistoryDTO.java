package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TourBookingHistoryDTO {
    private Long bookingId;
    private LocalDateTime bookingDate;
    private String bookingCode;
    private Long tourId;
    private String tourName;
    private String tourImage;
    private TourBookingStatus bookingStatus;
    private Double bookingTotalAmount;
    private LocalDateTime bookingExpiredAt;
}
