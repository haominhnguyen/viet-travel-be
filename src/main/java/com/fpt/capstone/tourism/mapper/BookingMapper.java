package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourScheduleDTO;
import com.fpt.capstone.tourism.dto.common.TourScheduleShortInfoDTO;
import com.fpt.capstone.tourism.dto.common.TourShortInfoDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourSchedule;

public interface BookingMapper {

    TourShortInfoDTO toTourShortInfoDTO(Tour tour);

    TourScheduleShortInfoDTO toTourScheduleShortInfoDTO(TourSchedule tourSchedule);


}
