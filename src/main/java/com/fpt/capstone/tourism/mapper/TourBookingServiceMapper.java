package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.ChangeServiceDTO;
import com.fpt.capstone.tourism.dto.common.ServiceProviderBookingServiceDTO;
import com.fpt.capstone.tourism.dto.common.TourBookingServiceCommonDTO;
import com.fpt.capstone.tourism.dto.common.TourDayDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingServiceDTO;
import com.fpt.capstone.tourism.model.TourBookingService;
import com.fpt.capstone.tourism.model.TourDay;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TourBookingServiceMapper  extends EntityMapper<TourBookingServiceDTO, TourBookingService>{
    TourBookingServiceCommonDTO toCommonDTO(TourBookingService entity);

    @Mapping(target = "tourName", source = "booking.tour.name")
    @Mapping(target = "tourType", expression = "java(entity.getBooking() != null ? entity.getBooking().getTour().getTourType().toString() : null)")
    @Mapping(target = "dayNumber", source = "tourDay.dayNumber")
    @Mapping(target = "bookingCode", source = "booking.bookingCode")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "proposer", expression = "java(entity.getBooking() != null && entity.getBooking().getUser() != null ? entity.getBooking().getUser().getFullName() : null)")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "tourBookingServiceId", source = "id")
    ChangeServiceDTO toChangeServiceDTO(TourBookingService entity);

    @Mapping(target = "serviceName", source = "service.name")
    ServiceProviderBookingServiceDTO toProviderBookingServiceDTO(TourBookingService entity);
}
