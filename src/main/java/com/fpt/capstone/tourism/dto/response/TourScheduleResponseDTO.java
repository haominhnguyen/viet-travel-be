package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.TourPaxDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourScheduleResponseDTO {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private TourPaxDTO tourPax;
    private UserBasicDTO tourGuide;
    private UserBasicDTO operator;
    private String meetingLocation;
    private LocalTime departureTime;
}
