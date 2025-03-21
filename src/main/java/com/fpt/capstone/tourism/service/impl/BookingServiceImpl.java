package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.CreatePublicBookingRequestDTO;
import com.fpt.capstone.tourism.dto.request.UpdateCustomersRequestDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import com.fpt.capstone.tourism.helper.IHelper.TourHelper;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.*;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.BookingService;
import com.fpt.capstone.tourism.service.TourBookingCustomerService;
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

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {


    private final TourRepository tourRepository;
    private final TourBookingRepository tourBookingRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TransactionRepository transactionRepository;
    private final TourBookingCustomerRepository tourBookingCustomerRepository;
    private final UserRepository userRepository;


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

            createReceiptBookingTransaction(result, bookingRequestDTO.getTotal(), bookingRequestDTO.getFullName(), bookingRequestDTO.getPaymentMethod());


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
    public GeneralResponse<PagingDTO<List<TourWithNumberBookingDTO>>> getTours(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection, TourType tourType) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<Tour> spec = tourHelper.buildTourPublicSearchSpecification(keyword, isDeleted, tourType);

            Page<Tour> tourPage = tourRepository.findAll(spec, pageable);

            return tourHelper.buildPublicTourPagedResponse(tourPage);
        } catch (Exception ex) {
            throw BusinessException.of("Get Data failed", ex);
        }
    }

    @Override
    public GeneralResponse<?> createBooking(CreatePublicBookingRequestDTO bookingRequestDTO) {


        try {
            List<TourBookingCustomer> customers = bookingRequestDTO.getCustomers().stream().map(bookingMapper::toTourBookingCustomer).toList();

            TourBooking tourBooking = TourBooking.builder()
                    .tour(Tour.builder().id(bookingRequestDTO.getTourId()).build())
                    .tourSchedule(TourSchedule.builder().id(bookingRequestDTO.getScheduleId()).build())
                    .seats(bookingRequestDTO.getCustomers().size())
                    .note(bookingRequestDTO.getNote())
                    .deleted(false)
                    .bookingCode(bookingHelper.generateBookingCode(bookingRequestDTO.getTourId(), bookingRequestDTO.getScheduleId(), bookingRequestDTO.getUserId()))
                    .user(User.builder().id(bookingRequestDTO.getUserId()).build())
                    .status(TourBookingStatus.SUCCESS)
                    .sellingPrice(bookingRequestDTO.getSellingPrice())
                    .extraHotelCost(bookingRequestDTO.getExtraHotelCost())
                    .tourBookingCategory(TourBookingCategory.SALE)
                    .paymentMethod(bookingRequestDTO.getPaymentMethod())
                    .sale(User.builder().id(bookingRequestDTO.getSaleId()).build())
                    .expiredAt(bookingRequestDTO.getExpiredAt())
                    .totalAmount(bookingRequestDTO.getTotalAmount())
                    .build();



            TourBooking result = tourBookingRepository.save(tourBooking);

            TourBookingCustomer bookedPerson = TourBookingCustomer.builder()
                    .ageType(AgeType.ADULT)
                    .fullName(bookingRequestDTO.getFullName())
                    .email(bookingRequestDTO.getEmail())
                    .phoneNumber(bookingRequestDTO.getPhone())
                    .deleted(false)
                    .bookedPerson(true)
                    .tourBooking(result)
                    .address(bookingRequestDTO.getAddress())
                    .build();

            // Save booking customers
            for (TourBookingCustomer customer : customers) {
                customer.setTourBooking(result);
            }


            tourBookingCustomerRepository.saveAll(customers);
            tourBookingCustomerRepository.save(bookedPerson);

            //Create transaction for booking
            createReceiptBookingTransaction(tourBooking, bookingRequestDTO.getTotalAmount(), bookingRequestDTO.getFullName(), bookingRequestDTO.getPaymentMethod());

            return GeneralResponse.of(bookingMapper.toBookingDetailSaleResponseDTO(result));
        } catch (Exception ex) {
            throw BusinessException.of("Create Public Booking failed!", ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourListBookings(Long tourId, Long scheduleId) {

        try {


            Tour tour = tourRepository.findById(tourId).orElseThrow();
            TourSchedule tourSchedule;
            if(scheduleId != null) {
                tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow();
            } else {
                tourSchedule = tour.getTourSchedules().get(0);
            }

            List<TourBooking> tourBookings = tourBookingRepository.findAllByTourAndTourSchedule(tour, tourSchedule);

            //List<TourBookingSaleResponseDTO> tourBookingSaleResponseDTOS = tourBookings.stream().map(bookingMapper::toTourBookingSaleResponseDTO).toList();

            List<TourBookingSaleResponseDTO> tourBookingSaleResponseDTOS = bookingHelper.setPaymentStatistics(tourBookings);

            TourDetailSaleResponseDTO tourDetailSaleResponseDTO = bookingMapper.toTourDetailSaleResponseDTO(tour);
            tourDetailSaleResponseDTO.setCreatedAt(tour.getCreatedAt());

            TourListBookingDTO tourListBookingDTO = TourListBookingDTO.builder()
                    .bookings(tourBookingSaleResponseDTOS)
                    .tour(tourDetailSaleResponseDTO)
                    .build();


            return GeneralResponse.of(tourListBookingDTO);

        }  catch (Exception ex) {
            throw BusinessException.of("Get Data failed", ex);
        }

    }

    @Override
    public GeneralResponse<?> saleViewBookingDetails(Long bookingId) {
        try {
            TourBooking tourBooking = tourBookingRepository.findById(bookingId).orElseThrow();
            return GeneralResponse.of(bookingHelper.setPaymentStatisticForBookingDetail(tourBooking));

        } catch (Exception ex) {
            throw BusinessException.of("Get Data failed", ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourBookingCustomers(Long bookingId) {
        try {
            List<TourBookingCustomer> tourBooking = tourBookingCustomerRepository.findByTourBookingId(bookingId);
            return GeneralResponse.of(tourBooking.stream().map(bookingMapper::toTourBookingCustomerDTO).toList());

        } catch (Exception ex) {
            throw BusinessException.of("Get Data failed", ex);
        }
    }

    @Override
    public GeneralResponse<?> changeCustomerStatus(Long customerId) {
        try {
            TourBookingCustomer tourBookingCustomer = tourBookingCustomerRepository.findById(customerId).orElseThrow();
            tourBookingCustomer.setDeleted(!tourBookingCustomer.getDeleted());
            TourBookingCustomer updatedTourBookingCustomer = tourBookingCustomerRepository.save(tourBookingCustomer);

            return GeneralResponse.of(bookingMapper.toTourBookingCustomerDTO(updatedTourBookingCustomer));
        } catch (Exception ex) {
            throw BusinessException.of("Update Customer Status failed", ex);
        }
    }

    @Override
    public GeneralResponse<?> updateCustomers(UpdateCustomersRequestDTO updateCustomersRequestDTO) {

        try {
            List<TourBookingCustomer> tourBookingCustomers = updateCustomersRequestDTO.getCustomers().stream().map(bookingMapper::toTourBookingCustomer).toList();
            TourBooking tourBooking = TourBooking.builder().id(updateCustomersRequestDTO.getBookingId()).build();
            for (TourBookingCustomer customer : tourBookingCustomers) {
                customer.setTourBooking(tourBooking);
            }
            List<TourBookingCustomer> updatedTourBookingCustomers = tourBookingCustomerRepository.saveAll(tourBookingCustomers);

            return GeneralResponse.of(updatedTourBookingCustomers.stream().map(bookingMapper::toTourBookingCustomerDTO).toList());
        } catch (Exception ex) {
            throw BusinessException.of("Update Customer Status failed", ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourDetails(Long tourId) {
        try {
            Tour tour = tourRepository.findById(tourId).orElseThrow();
            TourDetailSaleResponseDTO tourDetailSaleResponseDTO = bookingMapper.toTourDetailSaleResponseDTO(tour);
            return GeneralResponse.of(tourDetailSaleResponseDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Get tour details for sale failed", ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourDetails(Long tourId, Long scheduleId) {
        try {
            Tour tour = tourRepository.findById(tourId).orElseThrow();
            CreateBookingTourDTO tourDetailSaleResponseDTO = bookingMapper.toCreateBookingTourDTO(tour);
            PublicTourScheduleDTO scheduleDTO = tourScheduleRepository.findTourScheduleByTourId(tourId, scheduleId);
            tourDetailSaleResponseDTO.setTourSchedule(scheduleDTO);
            return GeneralResponse.of(tourDetailSaleResponseDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Get tour details for sale failed", ex);
        }
    }

    @Override
    public GeneralResponse<?> getCustomersByName(String name) {
        try {

            List<User> users = userRepository.findUsersByRoleNameAndFullNameLike("CUSTOMER", name);

            List<BookedCustomerDTO> customers = users.stream().map(bookingMapper::toBookedPersonDTO).toList();

            return GeneralResponse.of(customers);
        } catch (Exception ex) {
            throw BusinessException.of("Get customers for sale failed", ex);
        }
    }

    @Override
    public Transaction createReceiptBookingTransaction(TourBooking tourBooking, Double total, String fullName, PaymentMethod paymentMethod) {
        CostAccount costAccount = CostAccount.builder()
                .amount(total)
                .content("Customer pay for Booking Code: " + tourBooking.getBookingCode())
                .discount(0)
                .finalAmount(total)
                .quantity(1)
                .status(CostAccountStatus.PENDING)
                .build();

        List<CostAccount> costAccountList = new ArrayList<>();
        costAccountList.add(costAccount);

        Transaction transaction = Transaction.builder()
                .booking(tourBooking)
                .amount(costAccount.getAmount())
                .paymentMethod(paymentMethod)
                .category(TransactionType.RECEIPT)
                .costAccount(costAccountList)
                .paidBy(fullName)
                .transactionStatus(TransactionStatus.PENDING)
                .receivedBy("Viet Travel")
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    public void saveTourBookingService(TourBooking tourBooking) {
        Tour tour = tourBooking.getTour();
        List<TourDay> tourDays = tour.getTourDays();

    }
}
