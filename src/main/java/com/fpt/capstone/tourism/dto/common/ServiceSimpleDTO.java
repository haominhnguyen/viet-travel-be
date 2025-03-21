package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.ServiceCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceSimpleDTO {
    private Long id;
    private String name;
    private String serviceCategory;
}
