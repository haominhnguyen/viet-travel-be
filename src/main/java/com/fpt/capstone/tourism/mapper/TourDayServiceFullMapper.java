package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourDayServiceFullDTO;
import com.fpt.capstone.tourism.model.TourDayService;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourDayServiceFullMapper extends EntityMapper<TourDayServiceFullDTO, TourDayService> {
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "service.name", target = "serviceName")
    TourDayServiceFullDTO toDTO(TourDayService entity);

    @Mapping(target = "service.id", source = "serviceId")
    @Mapping(target = "tourDay", ignore = true)
    TourDayService toEntity(TourDayServiceFullDTO dto);

    default List<TourDayServiceFullDTO> toDTOList(List<TourDayService> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
