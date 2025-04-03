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
public class NewUsersChartDTO {
    private int month;         // Tháng (1 - 12)
    private int year;          // Năm (ví dụ: 2024)
    private long userCount; // Số tài khoản mới tạo trong tuần
}


