package com.fpt.capstone.tourism.mapper.impl;

import com.fpt.capstone.tourism.dto.common.TourScheduleShortInfoDTO;
import com.fpt.capstone.tourism.dto.common.TourShortInfoDTO;
import com.fpt.capstone.tourism.mapper.BookingMapper;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TagMapper;
import com.fpt.capstone.tourism.mapper.TourImageMapper;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingMapperImpl implements BookingMapper {

    private final LocationMapper locationMapper;
    private final TagMapper tagMapper;
    private final TourImageMapper tourImageMapper;


    @Override
    public TourShortInfoDTO toTourShortInfoDTO(Tour tour) {
        return TourShortInfoDTO.builder()
                .id(tour.getId())
                .name(tour.getName())
                .numberDays(tour.getNumberDays())
                .numberNight(tour.getNumberNights())
                .privacy(tour.getPrivacy())
                .tourImages(tourImageMapper.toPublicTourImageDTO(tour.getTourImages().get(0)))
                .depart_location(locationMapper.toPublicLocationDTO(tour.getDepartLocation()))
                .tags(tour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                .build();
    }

    @Override
    public TourScheduleShortInfoDTO toTourScheduleShortInfoDTO(TourSchedule tourSchedule) {
        return TourScheduleShortInfoDTO.builder()
                .scheduleId(tourSchedule.getId())
                .startDate(tourSchedule.getStartDate())
                .endDate(tourSchedule.getEndDate())
                .build();
    }
}
