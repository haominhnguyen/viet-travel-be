package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourShortInfoDTO {
    private Long id;
    private String name;
    private int numberDays;
    private int numberNights;
    private PublicTourImageDTO tourImage;
    private String privacy;
    private List<TagDTO> tags;
    private PublicLocationDTO departLocation;
}
