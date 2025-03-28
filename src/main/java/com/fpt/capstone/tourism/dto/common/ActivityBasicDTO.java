package com.fpt.capstone.tourism.dto.common;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityBasicDTO {
    private Long id;
    private String title;
    private Double pricePerPerson;
    private String imageUrl;
}
