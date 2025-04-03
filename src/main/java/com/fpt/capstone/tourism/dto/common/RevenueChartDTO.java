package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueChartDTO {
    private int month;         // Tháng (1 - 12)
    private int year;          // Năm (ví dụ: 2024)
    private BigDecimal revenue; // Tổng doanh thu tháng đó
}

