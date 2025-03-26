package com.fpt.capstone.tourism.dto.common;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDetailDTO {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private Integer dayNumber;
    private Long locationId;
    private String locationName;
    private Long categoryId;
    private String categoryName;
    private Double pricePerPerson;
    private Integer numberTicket;
    private Double latitude;
    private Double longitude;
}
