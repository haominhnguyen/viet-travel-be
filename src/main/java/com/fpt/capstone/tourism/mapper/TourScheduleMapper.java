package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.TourBookingSettlementResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourSettlementResponseDTO;
import com.fpt.capstone.tourism.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        TourMapper.class,
})
public interface TourScheduleMapper {
    @Mapping(source = "tour", target = "tour")
    @Mapping(source = "tourPax", target = "tourPax")
    @Mapping(source = "tourGuide", target = "tourGuide")
    @Mapping(source = "operator", target = "operator")
    @Mapping(source = "bookings", target = "bookings")
    TourSettlementResponseDTO toDTO(TourSchedule schedule);

    TourSupportInfoDTO toTourSupportInfoDTO(Tour tour);
    TourPaxOptionDTO toTourPaxDTO(TourPax tourPax);
    List<TransactionDTO> toTransactionDTOs(List<Transaction> transactions);

    @Mapping(source = "sale", target = "sale") // Map to StaffDTO
    @Mapping(target = "customer", expression = "java(mapBookedCustomer(booking.getCustomers()))")
    @Mapping(source = "transactions", target = "transactions")
    TourBookingSettlementResponseDTO toSettlementDTO(TourBooking booking);

    CostAccountDTO toCostAccountDTO(CostAccount costAccount);

    default BookedPersonDTO mapBookedCustomer(List<TourBookingCustomer> customers) {
        return customers == null ? null :
                customers.stream()
                        .filter(TourBookingCustomer::getBookedPerson)
                        .findFirst()
                        .map(this::toBookedPersonDTO)
                        .orElse(null);
    }

    BookedPersonDTO toBookedPersonDTO(TourBookingCustomer customer);
}
