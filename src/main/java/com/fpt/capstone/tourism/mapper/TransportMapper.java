package com.fpt.capstone.tourism.mapper;
import com.fpt.capstone.tourism.dto.common.TransportDTO;
import com.fpt.capstone.tourism.dto.common.TransportSimpleDTO;
import com.fpt.capstone.tourism.model.Transport;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransportMapper extends EntityMapper<TransportDTO, Transport> {
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "service.serviceCategory.id", target = "categoryId")
    @Mapping(source = "service.serviceCategory.categoryName", target = "categoryName")
    //@Mapping(source = "service.id", target = "serviceId")
    TransportDTO toDTO(Transport entity);

    TransportSimpleDTO toSimpleDTO(Transport entity);
}
