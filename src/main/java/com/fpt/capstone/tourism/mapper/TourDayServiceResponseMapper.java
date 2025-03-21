package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.response.TourDayServiceResponseDTO;
import com.fpt.capstone.tourism.model.TourDayService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ServiceMapper.class})
public interface TourDayServiceResponseMapper extends EntityMapper<TourDayServiceResponseDTO, TourDayService> {

    @Mapping(target = "tourDayId", source = "tourDay.id")
    TourDayServiceResponseDTO toDTO(TourDayService entity);
}

