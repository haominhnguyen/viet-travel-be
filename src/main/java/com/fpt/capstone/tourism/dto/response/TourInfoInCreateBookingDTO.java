package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TourInfoInCreateBookingDTO {
    private Long id;
    private String name;
    private int numberDays;
    private int numberNights;
    private TourType tourType;
    private TourStatus tourStatus;
    private LocationDTO departLocation;
    private PublicTourScheduleDTO tourSchedule;
    private LocalDateTime createdAt;
}
