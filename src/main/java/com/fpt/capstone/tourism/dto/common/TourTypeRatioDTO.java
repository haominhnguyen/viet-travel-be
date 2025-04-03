package com.fpt.capstone.tourism.dto.common;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourTypeRatioDTO {
    private int month;         // Tháng (1 - 12)
    private int year;          // Năm (ví dụ: 2024)
    private double sicRatio; // % tour SIC trong tháng đó
    private double privateRatio; // % tour Private trong tháng đó
}
