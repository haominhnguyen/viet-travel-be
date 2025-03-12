package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceFullDTO;
import com.fpt.capstone.tourism.dto.request.TourDayRequestDTO;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.TourDay;
import com.fpt.capstone.tourism.model.TourDayService;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourDayFullMapper extends EntityMapper<TourDayRequestDTO, TourDay> {

    @Mapping(target = "tourId", source = "tour.id")
    @Mapping(target = "tourDayServices", source = "tourDayServices")
    TourDayFullDTO toFullDTO(TourDay tourDay);

    @Named("toFullDTOList")
    default List<TourDayFullDTO> toFullDTOList(List<TourDay> tourDays) {
        if (tourDays == null) {
            return null;
        }
        return tourDays.stream().map(this::toFullDTO).collect(Collectors.toList());
    }

    @Mapping(target = "tour.id", source = "tourId")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "tourDayServices", ignore = true)
    TourDay toEntity(TourDayRequestDTO requestDTO);

    default LocationDTO toLocationDTO(Location location) {
        if (location == null) {
            return null;
        }
        return LocationDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .build();
    }

    default TourDayServiceFullDTO toTourDayServiceDTO(TourDayServiceFullDTO tourDayService) {
        if (tourDayService == null) {
            return null;
        }
        return TourDayServiceFullDTO.builder()
                .id(tourDayService.getId())
                .serviceId(tourDayService.getServiceId())
                .quantity(tourDayService.getQuantity())
                .sellingPrice(tourDayService.getSellingPrice())
                .build();
    }

    default Set<TourDayServiceFullDTO> toTourDayServiceDTOSet(Set<TourDayServiceFullDTO> tourDayServices) {
        if (tourDayServices == null) {
            return null;
        }
        return tourDayServices.stream()
                .map(this::toTourDayServiceDTO)
                .collect(Collectors.toSet());
    }

    default List<TourDayServiceFullDTO> toTourDayServiceDTOList(Set<TourDayServiceFullDTO> tourDayServices) {
        if (tourDayServices == null) {
            return null;
        }
        return tourDayServices.stream()
                .map(this::toTourDayServiceDTO)
                .collect(Collectors.toList());
    }
}
