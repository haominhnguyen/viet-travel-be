package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.response.TourDayResponseDTO;
import com.fpt.capstone.tourism.model.TourDay;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {LocationMapper.class, TourDayServiceMapper.class})
public interface TourDayResponseMapper extends EntityMapper<TourDayResponseDTO, TourDay> {

    @Mapping(source = "location", target = "location")
    @Mapping(source = "tourDayServices", target = "tourDayServices")
    TourDayResponseDTO toDTO(TourDay entity);

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "tour", ignore = true)
    TourDay toEntity(TourDayResponseDTO dto);

    default List<TourDayResponseDTO> toDTOList(List<TourDay> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }

        return entityList.stream()
                .filter(entity -> !Boolean.TRUE.equals(entity.getDeleted()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
