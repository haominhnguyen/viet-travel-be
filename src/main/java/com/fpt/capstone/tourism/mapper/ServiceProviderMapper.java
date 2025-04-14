package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.OperatorTransactionDTO;
import com.fpt.capstone.tourism.dto.common.ServiceProviderDTO;
import com.fpt.capstone.tourism.dto.common.ServiceProviderSimpleDTO;
import com.fpt.capstone.tourism.dto.response.PublicServiceProviderDTO;
import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceProviderMapper extends EntityMapper<ServiceProviderDTO, ServiceProvider> {
    PublicServiceProviderDTO toPublicServiceProviderDTO(ServiceProvider serviceProvider);


    ServiceProviderSimpleDTO toServiceProviderSimpleDTO(ServiceProvider serviceProvider);

//    @Mapping(target = "locationId", source = "location.id")
//    ServiceProviderDTO toServiceProviderDTO(ServiceProvider serviceProvider);

//    @Mapping(target = "locationId", expression = "java(getLocationId(serviceProvider))")
//    ServiceProviderDTO toDTO(ServiceProvider serviceProvider);

//    @Override
//    @Mapping(target = "location", source = "locationId")
//    ServiceProvider toEntity(ServiceProviderDTO dto);

    default Long getLocationId(ServiceProvider serviceProvider) {
        return Optional.ofNullable(serviceProvider)
                .map(ServiceProvider::getLocation)
                .map(Location::getId)
                .orElse(null);
    }


}


