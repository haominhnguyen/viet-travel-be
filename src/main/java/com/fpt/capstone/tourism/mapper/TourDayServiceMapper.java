package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourDayServiceDTO;
import com.fpt.capstone.tourism.model.TourDayService;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourDayServiceMapper {
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    List<TourDayServiceDTO> toDTOList(List<TourDayService> entities);
}

