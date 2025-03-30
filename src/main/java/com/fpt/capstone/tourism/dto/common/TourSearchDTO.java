package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.model.TourImage;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourSearchDTO {
    private Long id;
    private String name;
    private List<PublicTourImageDTO> tourImages;
}
