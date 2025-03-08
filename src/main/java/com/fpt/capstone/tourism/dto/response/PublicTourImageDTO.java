package com.fpt.capstone.tourism.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicTourImageDTO {
    private Long id;
    private String imageUrl;
}
