package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import jakarta.persistence.*;
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
}
