package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.dto.common.CostAccountDTO;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequestDTO {
    private String bookingCode;
    private String notes;
    private String paidBy;
    private PaymentMethod paymentMethod;
    private String receivedBy;
    private List<CostAccountDTO> costAccounts;
    private TransactionType category;
    private Double totalAmount;
}
