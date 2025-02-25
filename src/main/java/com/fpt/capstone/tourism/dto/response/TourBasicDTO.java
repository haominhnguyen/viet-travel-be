package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.TourDTO;
import com.fpt.capstone.tourism.model.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TourBasicDTO extends TourDTO {
    private Long id;
    private String name;
    private String highlights;
    private int numberDays;
    private int numberNight;
    private String note;
    private List<Location> locations;
    private List<Tag> tags;
    private Location depart_location;
    private List<TourSchedule> tourSchedules;
    private List<TourImage> tourImages;


// gia, depart-time, remain slot, depart calendar (khoi hanh ngay nao)
    //Schedule (lich trinh cho cac ngay cu the: di dau, may bua)
    //Other same location tour
    //gia nguoi lon, gia tre em, phu thu phong

}
