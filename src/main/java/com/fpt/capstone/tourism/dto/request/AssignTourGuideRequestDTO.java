package com.fpt.capstone.tourism.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class AssignTourGuideRequestDTO {
    private LocalTime departureTime;
    private Long tourGuideId;
    private String meetingLocation;
}
