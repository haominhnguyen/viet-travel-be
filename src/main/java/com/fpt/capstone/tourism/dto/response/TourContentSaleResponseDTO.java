package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.TagDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourContentSaleResponseDTO {
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
    private List<PublicTourDayDTO> tourDays;
}
