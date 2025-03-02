package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.request.LocationRequestDTO;
import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDayDTO;
import com.fpt.capstone.tourism.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationMapper extends EntityMapper<LocationDTO, Location>{
    Location toEntity(LocationRequestDTO requestDTO);
    PublicLocationDTO toPublicLocationDTO(Location location);
    Location toEntity(PublicLocationDTO publicLocationDTO);
}
