package com.fpt.capstone.tourism.mapper;


import com.fpt.capstone.tourism.dto.common.ServiceBaseDTO;
import com.fpt.capstone.tourism.model.Service;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceBaseMapper {
        @Mapping(source = "serviceCategory", target = "serviceCategory")
        @Mapping(source = "serviceProvider", target = "serviceProvider")
        @Mapping(source = "createdAt", target = "createdAt")
        @Mapping(source = "updatedAt", target = "updatedAt")
        ServiceBaseDTO toDTO(Service entity);
}




