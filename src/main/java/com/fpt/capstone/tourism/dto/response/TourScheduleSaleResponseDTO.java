package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.StaffDTO;
import com.fpt.capstone.tourism.dto.common.TourPaxDTO;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.TourScheduleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class TourScheduleSaleResponseDTO {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TourScheduleStatus status;
    private StaffDTO operator;
    private TourPaxDTO tourPax;
}
