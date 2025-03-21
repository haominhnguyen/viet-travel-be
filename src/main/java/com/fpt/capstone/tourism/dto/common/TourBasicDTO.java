package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import lombok.*;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourBasicDTO {
    private Long id;
    private String name;
    private String highlights;
    private int numberDays;
    private int numberNight;
    private String note;
    private Boolean deleted;
    private TourType tourType;
    private TourStatus tourStatus;
    private double markUpPercent;
    private String privacy;
    private Long createdUserId;
    private String createdUserName;
    private List<TourImageFullDTO> tourImages;
}
