package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDTO {
//    private Long id;----------
//    private String name;--------
//    private String highlights;---------
//    private int numberDays;-----
//    private int numberNight;---
//    private String note;-------
//    private Boolean deleted;
//    private List<Location> locations;---------
//    private List<Tag> tags;--------
//    private boolean open;
//    private Location depart_location;-------
//    private double markUpPercent;
//    private User createdBy;
//    private List<TourSchedule> tourSchedules;
//    private List<TourImage> tourImage;


    private Long id;
    private String name;
    private String highlights;
    private int numberDays;
    private int numberNights;
    private String note;
    private double markUpPercent;
    private TourType tourType;
    private TourStatus tourStatus;
    private List<LocationDTO> locations;
    private List<TagDTO> tags;
    private LocationDTO departLocation;
    private List<TourScheduleDTO> tourSchedules;
    private List<TourImageDTO> tourImages;
}
