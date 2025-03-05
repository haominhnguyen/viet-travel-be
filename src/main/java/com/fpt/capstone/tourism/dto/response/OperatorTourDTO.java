package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.TourScheduleDTO;
import com.fpt.capstone.tourism.model.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OperatorTourDTO {
    private Long scheduleId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String tourName;
    private String tourGuide;
    private String operator;
    private Integer maxPax;
    private Integer availableSeats;
}
