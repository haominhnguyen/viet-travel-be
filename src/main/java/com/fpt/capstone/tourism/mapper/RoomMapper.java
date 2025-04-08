package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.RoomDTO;
import com.fpt.capstone.tourism.dto.common.RoomSimpleDTO;
import com.fpt.capstone.tourism.model.Room;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper extends EntityMapper<RoomDTO, Room> {
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "service.serviceCategory.id", target = "categoryId")
    @Mapping(source = "service.serviceCategory.categoryName", target = "categoryName")
    //@Mapping(source = "service.id", target = "serviceId")
    RoomDTO toDTO(Room entity);
    RoomSimpleDTO toSimpleDTO(Room entity);
}
