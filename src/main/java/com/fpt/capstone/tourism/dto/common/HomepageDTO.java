package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PublicActivityDTO;
import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HomepageDTO {
    private PublicTourDTO topTourOfYear;
    private List<PublicTourDTO> trendingTours;
    private List<BlogResponseDTO> newBlogs;
    private List<PublicActivityDTO> recommendedActivities;
    private List<PublicLocationDTO> recommendedLocations;
}
