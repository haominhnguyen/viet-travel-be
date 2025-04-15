package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.request.TourDayAllRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourResponseDTO {
    private Long id;
    private String name;
    private String highlights;
    private Integer numberDays;
    private Integer numberNights;
    private String note;
    private List<PublicLocationDTO> locations;
    private List<TagDTO> tags;
    private String tourType;
    private String tourStatus;
    private PublicLocationDTO departLocation;
    private Double markUpPercent;
    private String privacy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private UserBasicDTO createdBy;
    private List<PublicTourImageDTO> tourImages;
}