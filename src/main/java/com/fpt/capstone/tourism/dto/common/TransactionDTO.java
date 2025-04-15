package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TransactionDTO {
    private Long id;
    private Double amount;
    private TransactionType category; // receipt hoặc payment
    private String paidBy; // Người trả tiền
    private String receivedBy; // Người nhận tiền
    private PaymentMethod paymentMethod;
    private String notes;
    private List<CostAccountDTO> costAccount;
    private LocalDateTime createdAt;
    private TransactionStatus transactionStatus;
}
