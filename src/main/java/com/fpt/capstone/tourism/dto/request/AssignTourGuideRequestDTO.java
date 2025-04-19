package com.fpt.capstone.tourism.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTourGuideRequestDTO {
    private LocalTime departureTime;
    private Long tourGuideId;
    private String meetingLocation;
}
