package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.model.TourImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourImageMapper {
    PublicTourImageDTO toPublicTourImageDTO(TourImage tourImage);
}
