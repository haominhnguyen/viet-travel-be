package com.fpt.capstone.tourism.dto.response;


import com.fpt.capstone.tourism.dto.common.BookedPersonDTO;
import com.fpt.capstone.tourism.dto.common.StaffDTO;
import com.fpt.capstone.tourism.dto.common.TourBookingServiceCommonDTO;
import com.fpt.capstone.tourism.dto.common.TransactionDTO;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TourBookingSettlementResponseDTO {
    private Long id;
    private int seats;
    private String note;
    private String bookingCode;
    private Boolean deleted;
    private StaffDTO sale;
    private BookedPersonDTO customer;
    private TourBookingStatus status;
    private TourBookingCategory tourBookingCategory;
    private PaymentMethod paymentMethod;
    private String reason;
    private LocalDateTime expiredAt;
    private List<TransactionDTO> transactions;
}
