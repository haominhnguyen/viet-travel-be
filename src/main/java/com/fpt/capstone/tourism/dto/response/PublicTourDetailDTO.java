package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PublicTourDetailDTO {
    private Long id;
    private String name;
    private String highlights;
    private int numberDays;
    private int numberNight;
    private String note;
    private String privacy;
    private List<PublicLocationDTO> locations;
    private List<TagDTO> tags;
    private PublicLocationDTO departLocation;
    private List<PublicTourScheduleDTO> tourSchedules;
    private List<PublicTourImageDTO> tourImages;
    private List<PublicTourDTO> otherTours;
    private List<PublicTourDayDTO> tourDays;

// gia, depart-time, remain slot, depart calendar (khoi hanh ngay nao)
    //Schedule (lich trinh cho cac ngay cu the: di dau, may bua)
    //Other same location tour
    //gia nguoi lon, gia tre em, phu thu phong

}
