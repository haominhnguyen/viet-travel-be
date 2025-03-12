package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperatorCostAccountDTO {
    private Long id;
    private String content;
    private Double amount; // Đơn giá
    private int discount;
    private int quantity;
    private Double finalAmount;
    private CostAccountStatus status;
}
