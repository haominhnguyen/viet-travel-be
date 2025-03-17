package com.fpt.capstone.tourism.mapper;


import com.fpt.capstone.tourism.dto.request.TourDayAllRequestDTO;
import com.fpt.capstone.tourism.model.TourDay;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourDayAllMapper  extends EntityMapper<TourDayAllRequestDTO, TourDay> {
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "tourDayServices", target = "tourDayServices")
    TourDayAllRequestDTO toDTO(TourDay entity);

    @Mapping(source = "locationId", target = "location.id")
    @Mapping(source = "tourDayServices", target = "tourDayServices")
    TourDay toEntity(TourDayAllRequestDTO dto);
}
