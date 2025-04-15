package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.TourDayPrivateRequestDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.mapper.custom.TourImageCustom;
import com.fpt.capstone.tourism.model.*;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        LocationMapper.class,
        TagMapper.class,
        TourImageMapper.class,
        TourImageCustom.class
})
public interface BookingMapper {
    @Mapping(target = "tourImage", source = "tourImages", qualifiedByName = { "TourImageTranslator", "mapFirstImage" })
    TourShortInfoDTO toTourShortInfoDTO(Tour tour);

    TourScheduleShortInfoDTO toTourScheduleShortInfoDTO(TourSchedule tourSchedule);

    @Mapping(target = "tour", expression = "java(mapTourWithoutPrivacy(tourBooking.getTour()))")
    TourBookingDTO toDto(TourBooking tourBooking);


    @Mapping(target = "tourSchedules", ignore = true)
    @Mapping(target = "tourImages", ignore = true)
    TourDTO toTourDTO(Tour tour);

    TourBookingDetailSaleResponseDTO toBookingDetailSaleResponseDTO(TourBooking tourBooking);



    TourDetailSaleResponseDTO toTourDetailSaleResponseDTO(Tour tour);

    @Mapping(target = "createdAt", source = "createdAt")
    TransactionDTO toTransactionDTO(Transaction transaction);


    TourPaxDTO toTourPaxDTO(TourPax tourPax);

    CostAccountDTO toCostAccountDTO(CostAccount costAccount);

    TourSupportInfoDTO toTourSupportInfoDTO(Tour tour);

    BookedCustomerDTO toBookedPersonDTO(User user);

    TourInfoInCreateBookingDTO toCreateBookingTourDTO(Tour tour);

    @Mapping(target = "paid", ignore = true)
    @Mapping(target = "total", ignore = true)
    TourBookingSaleResponseDTO toTourBookingSaleResponseDTO(TourBooking tourBooking);

    StaffDTO toStaffDto(User user);

    default TourShortInfoDTO mapTourWithoutPrivacy(Tour tour) {
        if (tour == null) {
            return null;
        }
        TourShortInfoDTO dto = toTourShortInfoDTO(tour); // ✅ Use existing method
        dto.setPrivacy(null); // ✅ Manually remove privacy
        return dto;
    }

    TourBookingCustomerDTO toTourBookingCustomerDTO(TourBookingCustomer customer);
    TourBookingCustomer toTourBookingCustomer(TourBookingCustomerDTO tourBookingCustomerDTO);

    ServiceSaleResponseDTO toServiceSaleResponseDTO(Service service);

    ServiceProviderSaleResponseDTO toServiceProviderSaleResponseDTO(ServiceProvider serviceProvider);

    TourDayDTO toTourDayDto(TourDay tourDay);

    ServiceCategoryDTO toServiceCategoryDto(ServiceCategory serviceCategory);

    TourBookingServiceDTO toTourBookingServiceDTO(TourBookingService tourBookingService);

    TourContentSaleResponseDTO toTourContentSaleResponseDTO(Tour tour);

    PublicTourDayDTO toPublicTourDayDTO(TourDay tourDay);


    @Mapping(source = "meals", target = "mealPlan")
    TourDay toEntity(TourDayPrivateRequestDTO tourDayPrivateRequestDTO);


    TourDayServiceCategoryDTO toTourDayServiceCategoryDTO(TourDayServiceCategory tourDayServiceCategory);

    SaleTourDayResponseDTO toSaleTourDayResponseDTO(TourDay tourDay);


    TourDayServiceCategorySaleResponseDTO toTourDayServiceCategorySaleResponseDTO(TourDayServiceCategory tourDayServiceCategory);

    ServiceCategoryWithTourDayResponseDTO toServiceCategoryWithTourDayResponseDTO(ServiceCategory serviceCategory);

    TourDayShortInfoDTO toTourDayShortInfoDTO(TourDay tourDay);

    ServiceProviderSimpleDTO toServiceProviderSimpleDTO(ServiceProvider serviceProvider);

    ServiceForTourPrivateDTO toServiceForTourPrivateDTO(Service service);

    TourDayServiceWithServiceOnlyDTO toTourDayServiceWithServiceOnlyDTO(TourDay tourDay);



}
