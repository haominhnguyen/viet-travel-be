package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.Tag;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class OperatorTourDetailDTO {
    private Long scheduleId;
    private String tourName;
    private String tourType;
    private List<TagDTO> tags;
    private Integer numberDays;
    private Integer numberNights;
    private String departureLocation;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private String createdBy;
    private Integer maxPax;
    private Integer soldSeats;
    private Integer pendingSeats;
    private Integer remainingSeats;
    private String operatorName;
    private LocalTime departureTime;
    private String tourGuideName;
    private String meetingLocation;
    private Double totalTourCost;           //Tổng chi phí tour
    private Double paidTourCost;            //Đã chi trong tour
    private Double remainingTourCost;       //Tiền còn lại của tour
    private Double revenueCost;             //Doanh thu tour (thu được bao tiền trong tour)
}
