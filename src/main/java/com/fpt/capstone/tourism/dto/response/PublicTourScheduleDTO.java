package com.fpt.capstone.tourism.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PublicTourScheduleDTO {
    private Long scheduleId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double sellingPrice;  // Giá bán tour cho lịch trình này
    private Integer minPax;  // Số người tối thiểu
    private Integer maxPax;  // Số người tối đa
    private Integer availableSeats; // Số chỗ còn lại
    private String meetingLocation;
    private LocalTime departureTime;
    private Double extraHotelCost;
}
