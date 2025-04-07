package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourDTO;
import com.fpt.capstone.tourism.dto.common.TourProcessDTO;
import com.fpt.capstone.tourism.dto.common.TourProcessDetailDTO;
import com.fpt.capstone.tourism.dto.common.TourScheduleSettlementDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingShortSaleResponseDTO;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourMapper extends EntityMapper<TourDTO, Tour>  {
//    @Mapping(target = "locationsId", source = "locations", qualifiedByName = "mapLocationIds")
//    @Mapping(target = "tagsId", source = "tags", qualifiedByName = "mapTagIds")
//    @Mapping(target = "departLocationId", source = "depart_location.id")
//    TourDTO toEntity(Tour entity);

    @Named("mapLocationIds")
    static List<Long> mapLocationIds(List<com.fpt.capstone.tourism.model.Location> locations) {
        return locations != null
                ? locations.stream().map(com.fpt.capstone.tourism.model.Location::getId).collect(Collectors.toList())
                : null;
    }

    @Named("mapTagIds")
    static List<Long> mapTagIds(List<com.fpt.capstone.tourism.model.Tag> tags) {
        return tags != null
                ? tags.stream().map(com.fpt.capstone.tourism.model.Tag::getId).collect(Collectors.toList())
                : null;
    }

    TourProcessDTO toTourProcessDTO(Tour tour);

    @Mapping(target = "createdBy", source = "createdBy.fullName")
    TourProcessDetailDTO toTourProcessDetailDTO(Tour tour);


    TourBookingShortSaleResponseDTO toTourBookingShortSaleResponseDTO(TourBooking tourBooking);

    @Mapping(source = "operator", target = "operator")
    TourScheduleSettlementDTO toTourScheduleSettlementDTO(TourSchedule tourSchedule);
}
