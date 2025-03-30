package com.fpt.capstone.tourism.dto.request;


import com.fpt.capstone.tourism.dto.common.CostAccountDTO;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UpdateTransactionRequestDTO {
    private Long id;
    private String receivedBy;
    private String paidBy;
    private PaymentMethod paymentMethod;
    private String notes;
    private List<CostAccountDTO> costAccounts;
    private Double totalAmount;
}
