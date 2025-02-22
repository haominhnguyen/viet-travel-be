package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.*;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourResponseDTO {
    private Long id;
    private String name;
    private String highlights;
    private int numberSeats;
    private int numberDays;
    private int numberNight;
    private String note;
    private Boolean deleted;
    private List<Location> locations;
    private List<Tag> tags;
    private Location depart_location;
    private User createdBy;
    private List<TourSchedule> tourSchedules;
    private List<TourImage> tourImages;
}
