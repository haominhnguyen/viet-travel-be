package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.request.ServiceCategoryRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceCategoryResponseDTO;
import com.fpt.capstone.tourism.model.ServiceCategory;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface ServiceCategoryDetailMapper {
    ServiceCategory toEntity(ServiceCategoryRequestDTO dto);
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    ServiceCategoryResponseDTO toDTO(ServiceCategory entity);
}
