package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingServiceSaleResponseDTO;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class TourBookingDetailSaleResponseDTO {
    private Long id;
    private int seats;
    private String note;
    private String bookingCode;
    private TourBookingStatus status;
    private TourBookingCategory tourBookingCategory;
    private String reason;
    private List<TourBookingCustomerDTO> customers;
    private double paid;
    private double total;
    private PaymentMethod paymentMethod;
    private List<TransactionDTO> transactions;
    private TourSupportInfoDTO tour;
    private PublicTourScheduleDTO schedule;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
}
