package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCreateUpdateRequestDTO {
    private Long dayId;
    private Long activityId;
    private Integer numberTicket;
    private String title;
    private String content;
    private String imageUrl;
    private Double pricePerPerson;
    private Long categoryId;
    private Long locationId;
    private Double latitude;
    private Double longitude;
}
