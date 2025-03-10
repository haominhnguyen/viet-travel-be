package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourDTO;
import com.fpt.capstone.tourism.dto.common.TourOperationLogDTO;
import com.fpt.capstone.tourism.dto.request.TourOperationLogRequestDTO;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourOperationLog;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourOperationLogMapper extends EntityMapper<TourOperationLogDTO, TourOperationLog> {
    TourOperationLog toEntity(TourOperationLogRequestDTO tourOperationLogRequestDTO);
}
