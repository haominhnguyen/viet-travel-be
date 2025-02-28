package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.ServiceDTO;
import com.fpt.capstone.tourism.model.Service;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceMapper {

    @Mapping(source = "serviceCategory.id", target = "categoryId")
    @Mapping(source = "serviceCategory.categoryName", target = "categoryName")
    @Mapping(source = "serviceProvider.id", target = "providerId")
    @Mapping(source = "serviceProvider.name", target = "providerName")
    ServiceDTO toDTO(Service entity);

    @Mapping(source = "categoryId", target = "serviceCategory.id")
    @Mapping(source = "providerId", target = "serviceProvider.id")
    Service toEntity(ServiceDTO dto);
}

