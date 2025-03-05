package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourShortInfoDTO {
    private Long id;
    private String name;
    private int numberDays;
    private int numberNight;
    private String privacy;
    private List<TagDTO> tags;
    private PublicLocationDTO depart_location;
    private PublicTourImageDTO tourImages;
}
