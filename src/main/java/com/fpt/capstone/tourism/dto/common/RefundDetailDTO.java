package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.TransactionStatus;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RefundDetailDTO {
    private Long id;
    private String name;
    private String bookingCode;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double amount;
    private String notes;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private String customerName;
    private String receivedBy;
    private String paidBy;
}
