package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourDayServiceDTO;
import com.fpt.capstone.tourism.dto.common.TourDayServiceFullDTO;
import com.fpt.capstone.tourism.model.TourDayService;
import org.mapstruct.*;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourDayServiceMapper {
    @Mapping(source = "tourDay", target = "tourDay")
    TourDayServiceDTO toDTO(TourDayService entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "sellingPrice", source = "sellingPrice")
    TourDayServiceFullDTO toTourDayServiceDTO(TourDayService tourDayService);
}


