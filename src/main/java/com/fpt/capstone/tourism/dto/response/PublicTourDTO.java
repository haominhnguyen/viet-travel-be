package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.common.TourImageDTO;
import com.fpt.capstone.tourism.dto.common.TourScheduleDTO;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PublicTourDTO {
    private Long id;
    private String name;
    private int numberDays;
    private int numberNight;
    private List<TagDTO> tags;
    private PublicLocationDTO departLocation;
    private List<PublicTourScheduleDTO> tourSchedules;
    private List<PublicTourImageDTO> tourImages;
    private Double priceFrom;
}
