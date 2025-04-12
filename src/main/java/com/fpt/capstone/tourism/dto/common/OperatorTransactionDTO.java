package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OperatorTransactionDTO {
    private Long id;
    private Double amount;
    private TransactionType category;
    private String paidBy;
    private String receivedBy;
    private PaymentMethod paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
    private List<OperatorCostAccountDTO> costAccount;
    private String paymentStatus;
}
