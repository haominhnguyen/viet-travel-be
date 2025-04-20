package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostAccountDTO {
    private Long id;
    private String content;
    private Double amount; // Đơn giá
    private int discount;
    private int quantity;
    private Double finalAmount;
    private CostAccountStatus status;
}
