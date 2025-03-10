package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class TourScheduleDTO {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TourPax tourPax;
    private Boolean deleted;
    private String status;
    private Tour tour;
    private User tourGuide;
    private User Operator;
    private String meetingLocation;
    private LocalTime departureTime;
    private Set<TourOperationLogDTO> operationLogs;
    private List<TourBooking> bookings;

}
