package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.TourPaxDTO;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourScheduleBasicResponseDTO {
    private Long id;
    private Long tourId;
    private String tourName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long operatorId;
    private String operatorName;
    private String status;
    private TourPaxDTO paxInfo;
}
