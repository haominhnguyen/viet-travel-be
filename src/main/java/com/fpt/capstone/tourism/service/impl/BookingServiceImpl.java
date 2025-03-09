package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import com.fpt.capstone.tourism.helper.IHelper.TourHelper;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.AgeType;
import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.BookingService;
import com.fpt.capstone.tourism.service.TourBookingCustomerService;
import com.fpt.capstone.tourism.service.TourService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    private final TourScheduleRepository tourScheduleRepository;
    private final TransactionRepository transactionRepository;
    private final TourBookingCustomerRepository tourBookingCustomerRepository;


    private final LocationMapper locationMapper;
    private final TourImageMapper tourImageMapper;
    private final TourBookingCustomerMapper tourBookingCustomerMapper;


    private final BookingHelper bookingHelper;
    private final BookingMapper bookingMapper;
    private final TourHelper tourHelper;

    private final TourBookingCustomerService tourBookingCustomerService;

    @Override
    public GeneralResponse<TourBookingDataResponseDTO> viewTourBookingDetail(Long tourId, Long scheduleId) {
        try{
            Tour currentTour = tourRepository.findById(tourId).orElseThrow();
            PublicTourScheduleDTO tourScheduleBasicDTO = tourScheduleRepository.findTourScheduleByTourId(tourId, scheduleId);

            //Mapping to DTO
            TourBookingDataResponseDTO tourBasicDTO = TourBookingDataResponseDTO.builder()
                    .id(currentTour.getId())
                    .name(currentTour.getName())
                    .numberDays(currentTour.getNumberDays())
                    .numberNight(currentTour.getNumberNights())
                    .privacy(currentTour.getPrivacy())
                    .departLocation(locationMapper.toPublicLocationDTO(currentTour.getDepartLocation()))
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
                    .tourBookingCategory(TourBookingCategory.ONLINE)
                    .paymentMethod(bookingRequestDTO.getPaymentMethod())
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
                    .content("Customer pay for Booking Code: " + result.getBookingCode())
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
                    .sellingPrice(tourBooking.getSellingPrice())
                    .extraHotelCost(tourBooking.getExtraHotelCost())
                    .children(children)
                    .note(tourBooking.getNote())
                    .bookingCode(tourBooking.getBookingCode())
                    .paymentMethod(tourBooking.getPaymentMethod())
                    .createdAt(tourBooking.getCreatedAt())
                    .paymentMethod(tourBooking.getPaymentMethod())
                    .build();

            return GeneralResponse.of(bookingConfirmResponse);
        } catch (Exception ex) {
            throw BusinessException.of(ex.getMessage(), ex);
        }
    }



    @Override
    public GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>> getTourBookings(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<TourBooking> spec = bookingHelper.buildSearchSpecification(keyword, isDeleted);

            Page<TourBooking> tourBookingPage = tourBookingRepository.findAll(spec, pageable);

            return bookingHelper.buildPagedResponse(tourBookingPage);
        } catch (Exception ex) {
            throw BusinessException.of("Get Data failed", ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<TourDTO>>> getPublicTours(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<Tour> spec = tourHelper.buildTourPublicSearchSpecification(keyword, isDeleted, true);

            Page<Tour> tourPage = tourRepository.findAll(spec, pageable);
            List<TourDTO> tourDTOS = tourPage.getContent().stream()
                    .map(bookingMapper::toTourDTO)
                    .toList();
            return tourHelper.buildPublicTourPagedResponse(tourPage, tourDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("Get Data failed", ex);
        }
    }
}
