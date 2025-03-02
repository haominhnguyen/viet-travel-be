package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.BookingRequestDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingDataResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TourBookingCustomerMapper;
import com.fpt.capstone.tourism.mapper.TourImageMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.TourBookingRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.repository.TourScheduleRepository;
import com.fpt.capstone.tourism.repository.TransactionRepository;
import com.fpt.capstone.tourism.service.BookingService;
import com.fpt.capstone.tourism.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {


    private final TourRepository tourRepository;
    private final TourBookingRepository tourBookingRepository;
    private final TourService tourService;
    private final TourScheduleRepository tourScheduleRepository;
    private final LocationMapper locationMapper;
    private final TourImageMapper tourImageMapper;
    private final TourBookingCustomerMapper tourBookingCustomerMapper;
    private final TransactionRepository transactionRepository;

    @Override
    public GeneralResponse<TourBookingDataResponseDTO> viewTourBookingDetail(Long tourId, Long scheduleId) {
        try{
            Tour currentTour = tourRepository.findById(tourId).orElseThrow();
            List<Long> locationIds = currentTour.getLocations().stream().map(location -> location.getId()).collect(Collectors.toList());
            List<PublicTourDTO> otherTour = tourService.findSameLocationPublicTour(locationIds);
            PublicTourScheduleDTO tourScheduleBasicDTO = tourScheduleRepository.findTourScheduleByTourId(tourId, scheduleId);

            //Mapping to DTO
            TourBookingDataResponseDTO tourBasicDTO = TourBookingDataResponseDTO.builder()
                    .id(currentTour.getId())
                    .name(currentTour.getName())
                    .numberDays(currentTour.getNumberDays())
                    .numberNight(currentTour.getNumberNight())
                    .privacy(currentTour.getPrivacy())
                    .depart_location(locationMapper.toPublicLocationDTO(currentTour.getDepart_location()))
                    .tourSchedules(tourScheduleBasicDTO)
                    .tourImage(tourImageMapper.toPublicTourImageDTO(currentTour.getTourImages().get(0)))
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Customer Tour Booking detail loaded successfully", tourBasicDTO);
        } catch (Exception ex){
            throw BusinessException.of("Customer Tour Booking detail loaded fail", ex);
        }

    }

    @Override
    public GeneralResponse<?> createBooking(BookingRequestDTO bookingRequestDTO) {
        try {
            List<TourBookingCustomer> adults = tourBookingCustomerMapper.toAdultEntity(bookingRequestDTO.getAdults());

            List<TourBookingCustomer> children = tourBookingCustomerMapper.toChildrenEntity(bookingRequestDTO.getChildren());


            List<TourBookingCustomer> allCustomers = new ArrayList<>();
            allCustomers.addAll(adults);
            allCustomers.addAll(children);


            TourBooking tourBooking = TourBooking.builder()
                    .tour(Tour.builder().id(bookingRequestDTO.getTourId()).build())
                    .tourSchedule(TourSchedule.builder().id(bookingRequestDTO.getScheduleId()).build())
                    .seats(bookingRequestDTO.getChildren().size() + bookingRequestDTO.getAdults().size())
                    .note(bookingRequestDTO.getNote())
                    .deleted(false)
                    .customers(allCustomers)
                    .user(User.builder().id(bookingRequestDTO.getUserId()).build())
                    .status(TourBookingStatus.PENDING)
                    .build();

            TourBooking result = tourBookingRepository.save(tourBooking);


            CostAccount costAccount = CostAccount.builder()
                    .amount(bookingRequestDTO.getTotal())
                    .content("Customer pay for Booking Tour ID: " + bookingRequestDTO.getTourId())
                    .discount(0)
                    .finalAmount(bookingRequestDTO.getTotal())
                    .quantity(1)
                    .build();

            List<CostAccount> costAccountList = new ArrayList<>();
            costAccountList.add(costAccount);

            Transaction transaction = Transaction.builder()
                    .booking(result)
                    .amount(costAccount.getAmount())
                    .paymentMethod(bookingRequestDTO.getPaymentMethod())
                    .category(TransactionType.RECEIPT)
                    .costAccount(costAccountList)
                    .paidBy(bookingRequestDTO.getFullName())
                    .receivedBy("Viet Travel")
                    .build();

            transactionRepository.save(transaction);


        return GeneralResponse.of("");

        } catch (Exception ex) {
            throw BusinessException.of(ex.getMessage(), ex);
        }
    }
}
