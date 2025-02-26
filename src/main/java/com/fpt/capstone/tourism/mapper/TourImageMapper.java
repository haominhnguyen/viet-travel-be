package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TourImageDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.model.TourImage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourImageMapper extends EntityMapper<TourImageDTO, TourImage> {
    PublicTourImageDTO toPublicTourImageDTO(TourImage tourImage);
}
