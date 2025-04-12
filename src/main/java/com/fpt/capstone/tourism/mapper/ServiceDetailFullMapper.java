//package com.fpt.capstone.tourism.mapper;
//
//import com.fpt.capstone.tourism.dto.request.ServiceDetailRequestDTO;
//import com.fpt.capstone.tourism.dto.response.ServiceDetailResponseDTO;
//import com.fpt.capstone.tourism.model.ServiceDetail;
//import org.mapstruct.*;
//
//import java.util.List;
//
//@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
//
//public interface ServiceDetailFullMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "service", ignore = true)
////    @Mapping(target = "createdAt", ignore = true)
////    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "deleted", source = "deleted")
//    ServiceDetail toEntity(ServiceDetailRequestDTO dto);
//
//    @Mapping(source = "service.id", target = "serviceId")
//    ServiceDetailResponseDTO toDTO(ServiceDetail entity);
//
//    List<ServiceDetailResponseDTO> toDTOList(List<ServiceDetail> entities);
//}
