package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicLocationSimpleDTO {
    private Long id;
    private String name;
}
