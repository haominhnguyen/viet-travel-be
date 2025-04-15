package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.TagDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDetailResponseDTO {
    private Long id;
    private String name;
    private String highlights;
    private Integer numberDays;
    private Integer numberNights;
    private String note;
    private String privacy;
    private List<PublicLocationDTO> locations;
    private List<TagDTO> tags;
    private PublicLocationDTO departLocation;
    private List<TourScheduleResponseDTO> tourSchedules;
    private List<PublicTourImageDTO> tourImages;
    private List<PublicTourDayDTO> tourDays;
}
