package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.ServiceFullDTO;
import com.fpt.capstone.tourism.model.Service;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ServiceDetailMapper.class, TourDayServiceMapper.class, ServiceCategoryFullMapper.class, ServiceProviderMapper.class})
public interface ServiceFullMapper {
    @Mapping(source = "serviceCategory", target = "serviceCategory")
    @Mapping(source = "serviceProvider", target = "serviceProvider")
    @Mapping(source = "serviceDetails", target = "serviceDetails")
    @Mapping(source = "tourDayServices", target = "tourDayServices")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    ServiceFullDTO toDTO(Service entity);
}
