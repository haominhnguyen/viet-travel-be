package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableServiceDTO {
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private Double nettPrice;
    private Double sellingPrice;
    private String status; // ACTIVE, EXPIRED, UPCOMING
    private Date startDate;
    private Date endDate;
}
