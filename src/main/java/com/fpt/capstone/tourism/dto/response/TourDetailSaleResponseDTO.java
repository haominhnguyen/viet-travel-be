package com.fpt.capstone.tourism.dto.response;


import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.common.TourImageDTO;
import com.fpt.capstone.tourism.dto.common.TourScheduleDTO;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TourDetailSaleResponseDTO {
    private Long id;
    private String name;
    private int numberDays;
    private int numberNights;
    private String note;
    private TourType tourType;
    private TourStatus tourStatus;
    private List<PublicLocationDTO> locations;
    private List<TagDTO> tags;
    private LocationDTO departLocation;
    private List<TourScheduleSaleResponseDTO> tourSchedules;
    private LocalDateTime createdAt;
}
