package com.fpt.capstone.tourism.mapper;


import com.fpt.capstone.tourism.dto.common.ServiceBaseDTO;
import com.fpt.capstone.tourism.model.Service;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceBaseMapper {

    @Mapping(source = "serviceCategory.id", target = "categoryId")
    @Mapping(source = "serviceCategory.categoryName", target = "categoryName")
    @Mapping(source = "serviceProvider.id", target = "providerId")
    @Mapping(source = "serviceProvider.name", target = "providerName")
    ServiceBaseDTO toDTO(Service entity);

    @Mapping(source = "categoryId", target = "serviceCategory.id")
    @Mapping(source = "providerId", target = "serviceProvider.id")
    Service toEntity(ServiceBaseDTO dto);
}




