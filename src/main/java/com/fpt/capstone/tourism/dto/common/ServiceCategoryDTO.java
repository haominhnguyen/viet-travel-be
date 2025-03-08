package com.fpt.capstone.tourism.dto.common;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryDTO {
    private Long id;
    private String categoryName;
    private Boolean deleted;
}
