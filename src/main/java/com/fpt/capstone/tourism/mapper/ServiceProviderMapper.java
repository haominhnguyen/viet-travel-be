package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.ServiceProviderDTO;
import com.fpt.capstone.tourism.dto.response.PublicServiceProviderDTO;
import com.fpt.capstone.tourism.model.ServiceProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceProviderMapper extends EntityMapper<ServiceProviderDTO, ServiceProvider> {

    PublicServiceProviderDTO toPublicServiceProviderDTO(ServiceProvider serviceProvider);

}


