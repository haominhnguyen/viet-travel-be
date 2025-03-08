package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.ServiceDetailDTO;
import com.fpt.capstone.tourism.model.ServiceDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceDetailMapper {
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    List<ServiceDetailDTO> toDTOList(List<ServiceDetail> entities);
}

