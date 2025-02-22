package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.*;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class TourDTO {
    //    private Long id;
//    private String name;
//    private String highlights;
//    private int numberSeats;
//    private int numberDays;
//    private int numberNight;
//    private String note;
//    private List<Long> locationsId;
//    private List<Long> tagsId;
//    private Long departLocationId;
    private Long id;
    private String name;
    private String highlights;
    private int numberSeats;
    private int numberDays;
    private int numberNight;
    private String note;
    private List<LocationDTO> locations;
    private List<TagDTO> tags;
    private LocationDTO depart_location;
    private List<TourScheduleDTO> tourSchedules;
    private List<TourImageDTO> tourImages;
}
