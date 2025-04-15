package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.dto.common.ServiceDTO;
import com.fpt.capstone.tourism.dto.common.ServiceSimpleDTO;
import com.fpt.capstone.tourism.dto.response.PublicActivityDTO;
import com.fpt.capstone.tourism.dto.response.PublicServiceDTO;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.ServiceCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceMapper extends EntityMapper<ServiceDTO, Service>{
    PublicServiceDTO toPublicServiceDTO(Service service);
    @Mapping(source = "serviceCategory.categoryName", target = "serviceCategory")
    ServiceSimpleDTO toSimpleDTO(Service service);

    @Mapping(source = "service.serviceProvider.location", target = "location")
    PublicActivityDTO toPublicActivityDTO(Service service);
}
