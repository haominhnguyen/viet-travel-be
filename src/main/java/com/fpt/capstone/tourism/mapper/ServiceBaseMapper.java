package com.fpt.capstone.tourism.mapper;


import com.fpt.capstone.tourism.dto.common.ServiceBaseDTO;
import com.fpt.capstone.tourism.dto.common.ServiceProviderSimpleDTO;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.ServiceProvider;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceBaseMapper {
        @Mapping(source = "serviceCategory.id", target = "serviceCategoryId")
        @Mapping(source = "serviceCategory.categoryName", target = "serviceCategoryName")
        @Mapping(source = "serviceProvider.id", target = "serviceProviderId")
        @Mapping(source = "serviceProvider.name", target = "serviceProviderName")
        @Mapping(source = "serviceProvider.abbreviation", target = "serviceProviderAbbreviation")
        @Mapping(source = "serviceProvider.imageUrl", target = "serviceProviderImageUrl")
        @Mapping(source = "createdAt", target = "createdAt")
        @Mapping(source = "updatedAt", target = "updatedAt")
        ServiceBaseDTO toDTO(Service entity);
}






