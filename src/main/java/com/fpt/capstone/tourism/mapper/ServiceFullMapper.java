package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.ServiceBaseDTO;
import com.fpt.capstone.tourism.dto.common.ServiceFullDTO;
import com.fpt.capstone.tourism.dto.request.ServiceRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceResponseDTO;
import com.fpt.capstone.tourism.model.Service;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {TourDayServiceMapper.class, ServiceCategoryFullMapper.class, ServiceProviderMapper.class})
public interface ServiceFullMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "serviceCategory", ignore = true)
    @Mapping(target = "serviceProvider", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "tourDayServices", ignore = true)
    Service toEntity(ServiceRequestDTO dto);

    @Mapping(target = "categoryId", source = "serviceCategory.id")
    @Mapping(target = "categoryName", source = "serviceCategory.categoryName")
    @Mapping(target = "providerId", source = "serviceProvider.id")
    @Mapping(target = "providerName", source = "serviceProvider.name")
    @Mapping(target = "roomDetails", ignore = true)
    @Mapping(target = "mealDetails", ignore = true)
    @Mapping(target = "transportDetails", ignore = true)
    ServiceResponseDTO toResponseDTO(Service entity);

    @Mapping(target = "serviceCategoryName", source = "serviceCategory.categoryName")
    ServiceBaseDTO toBaseDTO(Service entity);
}