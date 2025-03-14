package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingSaleResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourDetailSaleResponseDTO;
import com.fpt.capstone.tourism.mapper.custom.TourImageCustom;
import com.fpt.capstone.tourism.model.*;
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

    @Mapping(target = "tour", expression = "java(mapTourWithoutPrivacy(tourBooking.getTour()))")
    TourBookingDTO toDto(TourBooking tourBooking);


    @Mapping(target = "tourSchedules", ignore = true)
    @Mapping(target = "tourImages", ignore = true)
    TourDTO toTourDTO(Tour tour);


    TourDetailSaleResponseDTO toTourDetailSaleResponseDTO(Tour tour);

    @Mapping(target = "paid", ignore = true)
    @Mapping(target = "total", ignore = true)
    TourBookingSaleResponseDTO toTourBookingSaleResponseDTO(TourBooking tourBooking);

    StaffDTO toStaffDto(User user);

    default TourShortInfoDTO mapTourWithoutPrivacy(Tour tour) {
        if (tour == null) {
            return null;
        }
        TourShortInfoDTO dto = toTourShortInfoDTO(tour); // ✅ Use existing method
        dto.setPrivacy(null); // ✅ Manually remove privacy
        return dto;
    }


}
