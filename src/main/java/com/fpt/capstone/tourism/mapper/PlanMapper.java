package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.dto.response.PlanSaleResponseDTO;
import com.fpt.capstone.tourism.dto.response.UserBasicDTO;
import com.fpt.capstone.tourism.model.Plan;
import com.fpt.capstone.tourism.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlanMapper {
    PlanDTO toPlanDto(Plan plan);
    PlanSaleResponseDTO toPlanSaleResponseDTO(Plan plan);

    UserBasicDTO toUserBasicDTO(User user);
}
