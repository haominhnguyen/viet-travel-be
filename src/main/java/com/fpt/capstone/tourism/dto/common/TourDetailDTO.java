package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourDetailDTO {
    private Long id;
    private String name;
    private String highlights;
    private int numberDays;
    private int numberNight;
    private String note;
    private String privacy;
    private String tourType;
    private List<PublicLocationDTO> locations;
    private List<TagDTO> tags;
    private PublicLocationDTO departLocation;
    private List<PublicTourScheduleDTO> tourSchedules;
    private List<PublicTourImageDTO> tourImages;
    private List<PublicTourDayDTO> tourDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserBasicDTO createdBy;
}
