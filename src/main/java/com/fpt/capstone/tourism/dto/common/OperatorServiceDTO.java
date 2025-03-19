package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OperatorServiceDTO {
    private Long serviceId;
    private String bookingCode;
    private String serviceName;
    private String serviceCategory;
    private LocalDateTime usingDate;
    private Integer requestQuantity;
    private Integer currentQuantity;
    private String bookingStatus;
    private String paymentStatus;
}
