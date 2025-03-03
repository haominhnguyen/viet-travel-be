package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourDayServiceDTO;
import com.fpt.capstone.tourism.model.TourDayService;
import org.mapstruct.*;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourDayServiceMapper {
    @Mapping(source = "tourDay", target = "tourDay")
    TourDayServiceDTO toDTO(TourDayService entity);
}


