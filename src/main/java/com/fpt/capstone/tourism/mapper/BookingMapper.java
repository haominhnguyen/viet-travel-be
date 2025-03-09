package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourBookingDTO;
import com.fpt.capstone.tourism.dto.common.TourImageDTO;
import com.fpt.capstone.tourism.dto.common.TourScheduleShortInfoDTO;
import com.fpt.capstone.tourism.dto.common.TourShortInfoDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.mapper.custom.TourImageCustom;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourImage;
import com.fpt.capstone.tourism.model.TourSchedule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        LocationMapper.class,
        TagMapper.class,
        TourImageMapper.class,
        TourImageCustom.class
})
public interface BookingMapper {
    @Mapping(target = "tourImage", source = "tourImages", qualifiedByName = { "TourImageTranslator", "mapFirstImage" })
    TourShortInfoDTO toTourShortInfoDTO(Tour tour);

    TourScheduleShortInfoDTO toTourScheduleShortInfoDTO(TourSchedule tourSchedule);


    @Mapping(target = "tour.privacy", ignore = true)
    TourBookingDTO toDto(TourBooking tourBooking);


}
