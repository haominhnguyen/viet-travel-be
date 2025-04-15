package com.fpt.capstone.tourism.dto.common;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentBookingDTO {
    private Long bookingId;
    private String customerName;
    private String tourName;
    private BigDecimal totalAmount;
    private LocalDateTime bookingDate;
}

