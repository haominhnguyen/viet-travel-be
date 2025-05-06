package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
    @Min(value = 1, message = "Đơn giá phải dương")
    private Double amount; // Đơn giá
    private int discount;
    @Min(value = 1, message = "Số lượng phải dương")
    private int quantity;
    @Min(value = 1, message = "Số tiền phải dương")
    private Double finalAmount;
    private CostAccountStatus status;
}
