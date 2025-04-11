package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.model.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlanMapper {
    PlanDTO toPlanDto(Plan plan);
}
