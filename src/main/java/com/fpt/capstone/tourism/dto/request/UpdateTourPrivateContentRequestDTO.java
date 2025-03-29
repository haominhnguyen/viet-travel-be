package com.fpt.capstone.tourism.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UpdateTourPrivateContentRequestDTO {

    @NotNull
    private Long tourId;

    private Long tourScheduleId;

    @NotNull(message = "Start date cannot be null")
    private LocalDateTime startDate;

    @NotNull(message = "End date cannot be null")
    private LocalDateTime endDate;

    @NotBlank(message = "Highlights cannot be blank")
    private String highlights;

    @NotBlank(message = "Privacy setting cannot be blank")
    private String privacy;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @NotNull(message = "Tour days cannot be null")
    @Size(min = 1, message = "There must be at least one tour day")
    private List<TourDayPrivateRequestDTO> tourDays;
}
