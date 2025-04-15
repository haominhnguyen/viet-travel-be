package com.fpt.capstone.tourism.mapper;
import com.fpt.capstone.tourism.dto.common.MealDTO;
import com.fpt.capstone.tourism.dto.common.MealSimpleDTO;
import com.fpt.capstone.tourism.model.Meal;
import com.fpt.capstone.tourism.model.Service;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MealMapper {
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "service.serviceCategory.id", target = "categoryId")
    @Mapping(source = "service.serviceCategory.categoryName", target = "categoryName")
    MealDTO toDTO(Meal entity);

    MealSimpleDTO toSimpleDTO(Meal entity);

}