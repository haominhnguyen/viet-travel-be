package com.fpt.capstone.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourBookingDataResponseDTO {
    private Long id;
    private String name;
    private int numberDays;
    private int numberNight;
    private String privacy;
    private PublicLocationDTO departLocation;
    private PublicTourScheduleDTO tourSchedules;
    private PublicTourImageDTO tourImage;
}
