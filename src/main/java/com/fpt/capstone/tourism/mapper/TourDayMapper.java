package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.common.TourDayDTO;
import com.fpt.capstone.tourism.dto.common.TourDayProcessDetailDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDayDTO;
import com.fpt.capstone.tourism.model.Tag;
import com.fpt.capstone.tourism.model.TourDay;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourDayMapper extends EntityMapper<TourDayDTO, TourDay>{
    PublicTourDayDTO toPublicTourDayDTO(TourDay tourDay);

    TourDayProcessDetailDTO toTourDayProcessDetailDTO(TourDay tourDay);
}
