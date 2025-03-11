package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

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
}
