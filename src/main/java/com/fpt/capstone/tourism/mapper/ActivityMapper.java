package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.ActivityDTO;
import com.fpt.capstone.tourism.dto.response.PublicActivityDTO;
import com.fpt.capstone.tourism.model.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ActivityMapper extends EntityMapper<ActivityDTO, Activity>  {
    PublicActivityDTO toPublicActivityDTO(Activity activity);
}
