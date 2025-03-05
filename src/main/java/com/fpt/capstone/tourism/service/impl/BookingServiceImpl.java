package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.BookingConfirmResponse;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourScheduleDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingDataResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import com.fpt.capstone.tourism.mapper.BookingMapper;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.TourBookingCustomerMapper;
import com.fpt.capstone.tourism.mapper.TourImageMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.BookingService;
import com.fpt.capstone.tourism.service.TourBookingCustomerService;
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
    private final TourBookingCustomerService tourBookingCustomerService;
    private final BookingHelper bookingHelper;
    private final BookingMapper bookingMapper;
    private final TourBookingCustomerRepository tourBookingCustomerRepository;

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
                    .bookingCode(bookingHelper.generateBookingCode(bookingRequestDTO.getTourId(), bookingRequestDTO.getScheduleId(), bookingRequestDTO.getUserId()))
                    .user(User.builder().id(bookingRequestDTO.getUserId()).build())
                    .status(TourBookingStatus.PENDING)
                    .sellingPrice(bookingRequestDTO.getSellingPrice())
                    .extraHotelCost(bookingRequestDTO.getExtraHotelCost())
                    .build();



            TourBooking result = tourBookingRepository.save(tourBooking);


            TourBooking temp = TourBooking.builder().id(result.getId()).build();


            TourBookingCustomer bookedPerson = TourBookingCustomer.builder()
                    .ageType(AgeType.ADULT)
                    .fullName(bookingRequestDTO.getFullName())
                    .email(bookingRequestDTO.getEmail())
                    .phoneNumber(bookingRequestDTO.getPhone())
                    .deleted(false)
                    .bookedPerson(true)
                    .tourBooking(temp)
                    .address(bookingRequestDTO.getAddress())
                    .build();

            allCustomers.add(bookedPerson);

            for (TourBookingCustomer customer : allCustomers) {
                customer.setTourBooking(temp);
            }

            tourBookingCustomerService.saveAll(allCustomers);

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
                    .booking(temp)
                    .amount(costAccount.getAmount())
                    .paymentMethod(bookingRequestDTO.getPaymentMethod())
                    .category(TransactionType.RECEIPT)
                    .costAccount(costAccountList)
                    .paidBy(bookingRequestDTO.getFullName())
                    .receivedBy("Viet Travel")
                    .build();

            transactionRepository.save(transaction);


        return GeneralResponse.of(result.getBookingCode());

        } catch (Exception ex) {
            throw BusinessException.of(ex.getMessage(), ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourBookingDetails(String bookingCode) {
        try {
            TourBooking tourBooking = tourBookingRepository.findByBookingCode(bookingCode);


            TourShortInfoDTO tourShortInfoDTO = bookingMapper.toTourShortInfoDTO(tourBooking.getTour());
            TourScheduleShortInfoDTO tourScheduleShortInfoDTO = bookingMapper.toTourScheduleShortInfoDTO(tourScheduleRepository.findById(tourBooking.getTourSchedule().getId()).orElseThrow());

            TourBooking temp = TourBooking.builder().id(tourBooking.getId()).build();

            List<TourBookingCustomer> adultEntities = tourBookingCustomerRepository.findAllByTourBookingAndAgeTypeAndDeletedAndBookedPerson(temp, AgeType.ADULT, false, false);
            List<TourBookingCustomer> childrenEntities = tourBookingCustomerRepository.findAllByTourBookingAndAgeTypeAndDeletedAndBookedPerson(temp, AgeType.CHILDREN, false, false);

            TourBookingCustomer bookedPersonEntity = tourBookingCustomerRepository.findByTourBookingAndBookedPerson(tourBooking, true);


            List<TourCustomerDTO> adults = adultEntities.stream().map(tourBookingCustomerMapper::toTourCustomerDTO).toList();
            List<TourCustomerDTO> children = childrenEntities.stream().map(tourBookingCustomerMapper::toTourCustomerDTO).toList();

            BookingConfirmResponse bookingConfirmResponse = BookingConfirmResponse.builder()
                    .id(tourBooking.getId())
                    .bookedPerson(tourBookingCustomerMapper.toBookedPersonDTO(bookedPersonEntity))
                    .tour(tourShortInfoDTO)
                    .tourSchedule(tourScheduleShortInfoDTO)
                    .adults(adults)
                    .children(children)
                    .bookingCode(tourBooking.getBookingCode())
                    .createdAt(tourBooking.getCreatedAt())
                    .build();

            return GeneralResponse.of(bookingConfirmResponse);
        } catch (Exception ex) {
            throw BusinessException.of(ex.getMessage(), ex);
        }
    }
}
