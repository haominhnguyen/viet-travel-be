package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.enums.TourType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourSimpleDTO {
    private Long id;
    private String name;
    private String highlights;
    private int numberDays;
    private int numberNight;
    private String note;
    private Boolean deleted;
    private TourType tourType;
    private double markUpPercent;
    private String privacy;
    private Long createdUserId;
    private String createdUserName;
}
