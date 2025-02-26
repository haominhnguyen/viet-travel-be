package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.ServiceCategoryFullDTO;
import com.fpt.capstone.tourism.dto.common.ServiceProviderSimpleDTO;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.ServiceProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceCategoryFullMapper {

    @Mapping(source = "serviceProviders", target = "serviceProviders")
    ServiceCategoryFullDTO toDTO(ServiceCategory entity);

    @Mapping(source = "serviceProviders", target = "serviceProviders")
    ServiceCategory toEntity(ServiceCategoryFullDTO dto);

    List<ServiceProviderSimpleDTO> toSimpleProviderDTOList(List<ServiceProvider> providers);
}

