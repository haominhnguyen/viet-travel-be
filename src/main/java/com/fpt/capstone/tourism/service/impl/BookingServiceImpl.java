package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.constants.Constants;
import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import com.fpt.capstone.tourism.helper.IHelper.TourHelper;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.*;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.BookingService;
import com.fpt.capstone.tourism.service.EmailService;
import com.fpt.capstone.tourism.service.TourBookingCustomerService;
import com.fpt.capstone.tourism.service.VNPayService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.EMAIL_ALREADY_EXISTS_MESSAGE;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.USERNAME_ALREADY_EXISTS_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {


    private final TourRepository tourRepository;
    private final TourBookingRepository tourBookingRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TransactionRepository transactionRepository;
    private final TourBookingCustomerRepository tourBookingCustomerRepository;
    private final UserRepository userRepository;
    private final TourBookingServiceRepository tourBookingServiceRepository;
    private final TourDayRepository tourDayRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final TourDayServiceCategoryRepository tourDayServiceCategoryRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final UserRoleRepository userRoleRepository;


    private final VNPayService vnPayService;


    private final LocationMapper locationMapper;
    private final TourImageMapper tourImageMapper;
    private final TourBookingCustomerMapper tourBookingCustomerMapper;
    private final UserFullInformationMapper userFullInformationMapper;


    private final BookingHelper bookingHelper;
    private final BookingMapper bookingMapper;
    private final TourHelper tourHelper;

    private final TourBookingCustomerService tourBookingCustomerService;
    private final CostAccountRepository costAccountRepository;
    private final LocationRepository locationRepository;
    private final TourPaxRepository tourPaxRepository;
    private final TourDayServiceRepository tourDayServiceRepository;
    private final TourImageRepository tourImageRepository;

    private final UserServiceImpl userService;
    private final EmailService emailService;


    @Value("${backend.base-url}")
    private String backendBaseUrl;

    @Override
    public GeneralResponse<TourBookingDataResponseDTO> viewTourBookingDetail(Long tourId, Long scheduleId) {
        try {
            log.info("Start find tour  with ID: {}", tourId);
            Tour currentTour = tourRepository.findTourByTourId(tourId);
            log.info("Start find tour  with ID: {}", tourId);
            List<TourImage> tourImages = tourImageRepository.findTourImagesByTourId(currentTour.getId());
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
                    .tourImage(tourImageMapper.toPublicTourImageDTO(tourImages.get(0)))
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_BOOKING_DETAIL_LOAD_SUCCESS, tourBasicDTO);
        } catch (Exception ex) {
            throw BusinessException.of(TOUR_BOOKING_DETAIL_LOAD_FAIL, ex);
        }

    }

    @Override
    @Transactional
    public GeneralResponse<?> createBooking(BookingRequestDTO bookingRequestDTO) {
        try {
            List<TourBookingCustomer> adults = tourBookingCustomerMapper.toAdultEntity(bookingRequestDTO.getAdults());

            List<TourBookingCustomer> children = tourBookingCustomerMapper.toChildrenEntity(bookingRequestDTO.getChildren());


            List<TourBookingCustomer> allCustomers = new ArrayList<>();
            allCustomers.addAll(adults);
            allCustomers.addAll(children);

            String baseUrl = backendBaseUrl + "/public/booking";

            String bookingCode = bookingHelper.generateBookingCode(bookingRequestDTO.getTourId(), bookingRequestDTO.getScheduleId(), bookingRequestDTO.getUserId());

            String paymentUrl = vnPayService.generatePaymentUrl(bookingRequestDTO.getTotal(), bookingCode, baseUrl);

            TourBooking tourBooking = TourBooking.builder()
                    .tour(Tour.builder().id(bookingRequestDTO.getTourId()).build())
                    .tourSchedule(TourSchedule.builder().id(bookingRequestDTO.getScheduleId()).build())
                    .seats(bookingRequestDTO.getChildren().size() + bookingRequestDTO.getAdults().size())
                    .note(bookingRequestDTO.getNote())
                    .deleted(false)
                    .bookingCode(bookingCode)
                    .user(User.builder().id(bookingRequestDTO.getUserId()).build())
                    .status(TourBookingStatus.PENDING)
                    .sellingPrice(bookingRequestDTO.getSellingPrice())
                    .extraHotelCost(bookingRequestDTO.getExtraHotelCost())
                    .tourBookingCategory(TourBookingCategory.ONLINE)
                    .paymentMethod(bookingRequestDTO.getPaymentMethod())
                    .paymentUrl(paymentUrl)
                    .expiredAt(LocalDateTime.now().plusHours(9))
                    .totalAmount(bookingRequestDTO.getTotal())
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

            saveTourBookingService(result);

            createReceiptBookingTransaction(result, bookingRequestDTO.getTotal(), bookingRequestDTO.getFullName(), bookingRequestDTO.getPaymentMethod());


            return GeneralResponse.of(result.getBookingCode());

        } catch (Exception ex) {
            throw BusinessException.of("Thất bại", ex);
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
                    .paymentUrl(tourBooking.getPaymentUrl())
                    .status(tourBooking.getStatus())
                    .build();

            return GeneralResponse.of(bookingConfirmResponse);
        } catch (Exception ex) {
            throw BusinessException.of("Thất bại", ex);
        }
    }


    @Override
    public GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>> getTourBookings(int page, int size, String keyword, String status, String sortField, String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<TourBooking> spec = bookingHelper.buildSearchSpecification(keyword, status);

            Page<TourBooking> tourBookingPage = tourBookingRepository.findAll(spec, pageable);

            return bookingHelper.buildPagedResponse(tourBookingPage);
        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<TourWithNumberBookingDTO>>> getTours(int page, int size, String keyword, TourStatus status, String sortField, String sortDirection, TourType tourType) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<Tour> spec = tourHelper.buildTourPublicSearchSpecification(keyword, status, tourType);

            Page<Tour> tourPage = tourRepository.findAll(spec, pageable);

            return tourHelper.buildPublicTourPagedResponse(tourPage);
        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> createBooking(CreatePublicBookingRequestDTO bookingRequestDTO) {
        try {
            List<TourBookingCustomer> customers = bookingRequestDTO.getCustomers().stream().map(bookingMapper::toTourBookingCustomer).toList();

            String baseUrl = backendBaseUrl + "/public/booking";

            String bookingCode = bookingHelper.generateBookingCode(bookingRequestDTO.getTourId(), bookingRequestDTO.getScheduleId(), bookingRequestDTO.getUserId());

            String paymentUrl = vnPayService.generatePaymentUrl(bookingRequestDTO.getTotalAmount(), bookingCode, baseUrl);

            TourBooking tourBooking = TourBooking.builder()
                    .tour(Tour.builder().id(bookingRequestDTO.getTourId()).build())
                    .tourSchedule(TourSchedule.builder().id(bookingRequestDTO.getScheduleId()).build())
                    .seats(bookingRequestDTO.getCustomers().size())
                    .note(bookingRequestDTO.getNote())
                    .deleted(false)
                    .bookingCode(bookingCode)
                    .user(User.builder().id(bookingRequestDTO.getUserId()).build())
                    .status(TourBookingStatus.PENDING)
                    .sellingPrice(bookingRequestDTO.getSellingPrice())
                    .extraHotelCost(bookingRequestDTO.getExtraHotelCost())
                    .tourBookingCategory(TourBookingCategory.SALE)
                    .paymentMethod(bookingRequestDTO.getPaymentMethod())
                    .sale(User.builder().id(bookingRequestDTO.getSaleId()).build())
                    .expiredAt(bookingRequestDTO.getExpiredAt())
                    .totalAmount(bookingRequestDTO.getTotalAmount())
                    .paymentUrl(paymentUrl)
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

            saveTourBookingService(result);

            //Create transaction for booking
            createReceiptBookingTransaction(result, bookingRequestDTO.getTotalAmount(), bookingRequestDTO.getFullName(), bookingRequestDTO.getPaymentMethod());

            return GeneralResponse.of(bookingMapper.toBookingDetailSaleResponseDTO(result));
        } catch (Exception ex) {
            throw BusinessException.of(CREATE_PUBLIC_BOOKING_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourListBookings(Long tourId, Long scheduleId) {
        try {
            Tour tour = tourRepository.findById(tourId).orElseThrow();
            TourSchedule tourSchedule = new TourSchedule();
            if (scheduleId != null) {
                tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow();
            } else {
                for(TourSchedule schedule : tour.getTourSchedules()) {
                    if(!schedule.getStatus().toString().equalsIgnoreCase(TourScheduleStatus.DRAFT.toString())
                    && !schedule.getStatus().toString().equalsIgnoreCase(TourScheduleStatus.CANCELLED.toString()
                    )) {
                        tourSchedule = schedule;
                        break;
                    }
                }

            }

            List<TourBooking> tourBookings = tourBookingRepository.findAllByTourAndTourSchedule(tour, tourSchedule);

            //List<TourBookingSaleResponseDTO> tourBookingSaleResponseDTOS = tourBookings.stream().map(bookingMapper::toTourBookingSaleResponseDTO).toList();

            List<TourBookingSaleResponseDTO> tourBookingSaleResponseDTOS = bookingHelper.setPaymentStatistics(tourBookings);

            TourDetailSaleResponseDTO tourDetailSaleResponseDTO = bookingMapper.toTourDetailSaleResponseDTO(tour);
            tourDetailSaleResponseDTO.setCreatedAt(tour.getCreatedAt());


            List<TourScheduleSaleResponseDTO> dtos = tourDetailSaleResponseDTO.getTourSchedules();
            dtos.removeIf(dto -> dto.getStatus().toString().equalsIgnoreCase(TourScheduleStatus.CANCELLED.toString()) || dto.getStatus().toString().equalsIgnoreCase(TourScheduleStatus.DRAFT.toString()));


            tourDetailSaleResponseDTO.setTourSchedules(dtos);

            TourListBookingDTO tourListBookingDTO = TourListBookingDTO.builder()
                    .bookings(tourBookingSaleResponseDTOS)
                    .tour(tourDetailSaleResponseDTO)
                    .build();


            return GeneralResponse.of(tourListBookingDTO);

        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }

    }

    @Override
    public GeneralResponse<?> saleViewBookingDetails(Long bookingId) {
        try {
            log.info("Start find tour booking detail with ID: {}", bookingId);
            TourBooking tourBooking = tourBookingRepository.findByBookingId(bookingId);
            log.info("End find tour booking detail with ID: {}", bookingId);

            return GeneralResponse.of(bookingHelper.setPaymentStatisticForBookingDetail(tourBooking));

        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourBookingCustomers(Long bookingId) {
        try {
            List<TourBookingCustomer> tourBooking = tourBookingCustomerRepository.findByTourBookingId(bookingId);
            return GeneralResponse.of(tourBooking.stream().map(bookingMapper::toTourBookingCustomerDTO).toList());

        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public GeneralResponse<?> changeCustomerStatus(Long customerId) {
        try {
            TourBookingCustomer tourBookingCustomer = tourBookingCustomerRepository.findById(customerId).orElseThrow();
            tourBookingCustomer.setDeleted(!tourBookingCustomer.getDeleted());
            TourBookingCustomer updatedTourBookingCustomer = tourBookingCustomerRepository.save(tourBookingCustomer);

            int modifiedQuantity = tourBookingCustomer.getDeleted() ? -1 : 1;

            TourBooking tourBooking = tourBookingRepository.findById(tourBookingCustomer.getTourBooking().getId()).orElseThrow();
            tourBooking.setSeats(tourBooking.getSeats() + modifiedQuantity);
            tourBookingRepository.save(tourBooking);

            return GeneralResponse.of(bookingMapper.toTourBookingCustomerDTO(updatedTourBookingCustomer));
        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_CUSTOMER_STATUS_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> updateCustomers(UpdateCustomersRequestDTO updateCustomersRequestDTO) {

        try {
            List<TourBookingCustomer> tourBookingCustomers = updateCustomersRequestDTO.getCustomers().stream().map(bookingMapper::toTourBookingCustomer).toList();
            TourBooking tourBooking = tourBookingRepository.findById(updateCustomersRequestDTO.getBookingId()).orElseThrow();

            int totalCustomer = 0;

            for (TourBookingCustomer customer : tourBookingCustomers) {
                customer.setTourBooking(tourBooking);
                if (!customer.getDeleted()) {
                    totalCustomer++;
                }
            }

            tourBooking.setSeats(totalCustomer);

            tourBookingRepository.save(tourBooking);

            List<TourBookingCustomer> updatedTourBookingCustomers = tourBookingCustomerRepository.saveAll(tourBookingCustomers);

            return GeneralResponse.of(updatedTourBookingCustomers.stream().map(bookingMapper::toTourBookingCustomerDTO).toList());
        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_CUSTOMER_STATUS_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourDetails(Long tourId) {
        try {
            Tour tour = tourRepository.findById(tourId).orElseThrow();
            TourDetailSaleResponseDTO tourDetailSaleResponseDTO = bookingMapper.toTourDetailSaleResponseDTO(tour);
            return GeneralResponse.of(tourDetailSaleResponseDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_DETAILS_FOR_SALE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourDetails(Long tourId, Long scheduleId) {
        try {
            Tour tour = tourRepository.findById(tourId).orElseThrow();
            TourInfoInCreateBookingDTO tourDetailSaleResponseDTO = bookingMapper.toCreateBookingTourDTO(tour);
            PublicTourScheduleDTO scheduleDTO = tourScheduleRepository.findTourScheduleByTourId(tourId, scheduleId);
            tourDetailSaleResponseDTO.setTourSchedule(scheduleDTO);
            return GeneralResponse.of(tourDetailSaleResponseDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_DETAILS_FOR_SALE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getCustomersByName(String name) {
        try {

            List<User> users = userRepository.findUsersByRoleNameAndFullNameLike("CUSTOMER", name);

            List<BookedCustomerDTO> customers = users.stream().map(bookingMapper::toBookedPersonDTO).toList();

            return GeneralResponse.of(customers);
        } catch (Exception ex) {
            throw BusinessException.of(GET_CUSTOMERS_FOR_SALE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> updateTourBookingService(Long tourBookingServiceID) {
        return null;
    }

    @Override
    public Transaction createReceiptBookingTransaction(TourBooking tourBooking, Double total, String fullName, PaymentMethod paymentMethod) {

        Transaction transaction = Transaction.builder()
                .booking(tourBooking)
                .amount(total)
                .notes("Customer pay for Booking Code: " + tourBooking.getBookingCode())
                .paymentMethod(paymentMethod)
                .category(TransactionType.RECEIPT)
                .paidBy(fullName)
                .transactionStatus(TransactionStatus.PENDING)
                .receivedBy("Viet Travel")
                .build();

        Transaction result = transactionRepository.save(transaction);

        CostAccount costAccount = CostAccount.builder()
                .amount(total)
                .transaction(result)
                .content("Customer pay for Booking Code: " + tourBooking.getBookingCode())
                .discount(0)
                .finalAmount(total)
                .quantity(1)
                .status(CostAccountStatus.PENDING)
                .build();

        costAccountRepository.save(costAccount);

        return transactionRepository.save(transaction);
    }

    @Override
    public void saveTourBookingService(TourBooking tourBooking) {
        Tour tour = tourBooking.getTour();
        List<TourDay> tourDays = tourDayRepository.findAllByTourId(tour.getId());

        TourType tourType = tourRepository.getTourTypeByTourId(tour.getId());

        List<TourBookingCustomer> customers = tourBookingCustomerRepository.findByBookedPersonAndTourBooking(false, tourBooking);

        int totalRooms = calculateTotalRooms(customers);

        for (TourDay tourDay : tourDays) {
            List<TourDayService> dayServices = tourDay.getTourDayServices();
            for (TourDayService dayService : dayServices) {
                TourBookingService tourBookingService = TourBookingService.builder()
                        .booking(tourBooking)
                        .tourDay(tourDay)
                        .service(dayService.getService())
                        .deleted(false)
                        .build();

                if(tourType.toString().equalsIgnoreCase("SIC")) {
                    tourBookingService.setStatus(TourBookingServiceStatus.AVAILABLE);
                } else {
                    tourBookingService.setStatus(TourBookingServiceStatus.NOT_ORDERED);
                }

                com.fpt.capstone.tourism.model.Service service = serviceRepository.findById(dayService.getService().getId()).orElseThrow();

                if (service.getServiceCategory().getCategoryName().equals("Hotel")) {
                    tourBookingService.setCurrentQuantity(totalRooms);
                } else if (service.getServiceCategory().getCategoryName().equals("Restaurant")) {
                    tourBookingService.setCurrentQuantity(customers.size());
                } else if (service.getServiceCategory().getCategoryName().equals("Activity")) {
                    tourBookingService.setCurrentQuantity(customers.size());
                } else if (service.getServiceCategory().getCategoryName().equals("Flight Ticket")) {
                    tourBookingService.setCurrentQuantity(customers.size());
                }else if (service.getServiceCategory().getCategoryName().equals("Transport")) {
                    tourBookingService.setCurrentQuantity(customers.size());
                }
                tourBookingServiceRepository.save(tourBookingService);
            }
        }
    }

    public int calculateTotalRooms(List<TourBookingCustomer> customers) {
        int singleRooms = 0;
        int availableForDoubleRooms = 0;

        for (TourBookingCustomer customer : customers) {
            if (Boolean.TRUE.equals(customer.getSingleRoom())) {
                singleRooms++;
            } else if (customer.getAgeType() == AgeType.ADULT) {
                availableForDoubleRooms++;
            }
        }

        int doubleRooms = availableForDoubleRooms / 2;
        int leftover = availableForDoubleRooms % 2;

        singleRooms += leftover;

        return singleRooms + doubleRooms;
    }

    @Override
    public GeneralResponse<?> getTourBookingServices(Long tourBookingID) {
        try {
            log.info("Start get tour booking service by booking ID: {}", tourBookingID);
            TourBooking tourBooking = tourBookingRepository.findByBookingId(tourBookingID);
            if (Objects.nonNull(tourBooking)) {
                Tour tour = tourBooking.getTour();
                List<TourDay> tourDays = tourDayRepository.findAllByTourId(tour.getId());
                TourType tourType = tourRepository.getTourTypeByTourId(tour.getId());
                List<TourBookingServiceSaleResponseDTO> responseLst = bookingHelper.getTourBookingListService(tourDays, tourBooking);

                SaleTourBookingServiceListResponseDTO dto = SaleTourBookingServiceListResponseDTO.builder()
                        .servicesByDay(responseLst)
                        .tourType(tourType)
                        .build();



                log.info("End get tour booking service by booking ID: {}", tourBookingID);
                return GeneralResponse.of(dto);
            }
            log.info("Not exist tour booking service with tour booking ID: {}", tourBookingID);
            return GeneralResponse.of(Collections.emptyList());

        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_BOOKING_SERVICES_FOR_SALE_FAIL, ex);
        }

    }

    @Override
    public GeneralResponse<?> updateServiceQuantity(UpdateServiceNotBookingSaleRequestDTO updateServiceNotBookingSaleRequestDTO) {
        try {
            TourBookingService tourBookingService = tourBookingServiceRepository.findById(updateServiceNotBookingSaleRequestDTO.getTourBookingServiceId()).orElseThrow();
            tourBookingService.setCurrentQuantity(updateServiceNotBookingSaleRequestDTO.getCurrentQuantity());
            TourBookingService updatedTourBookingService = tourBookingServiceRepository.save(tourBookingService);
            return GeneralResponse.of(bookingMapper.toTourBookingServiceDTO(updatedTourBookingService));
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_BOOKING_SERVICES_FOR_SALE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> cancelService(Long tourBookingServiceId) {
        try {
            TourBookingService tourBookingService = tourBookingServiceRepository.findById(tourBookingServiceId).orElseThrow();

            if(tourBookingService.getStatus().equals(TourBookingServiceStatus.NOT_ORDERED)) {
                tourBookingService.setStatus(TourBookingServiceStatus.CANCELLED);
            } else {
                tourBookingService.setStatus(TourBookingServiceStatus.CANCEL_REQUEST);
            }
            TourBookingService updatedTourBookingService = tourBookingServiceRepository.save(tourBookingService);
            return GeneralResponse.of(bookingMapper.toTourBookingServiceDTO(updatedTourBookingService));
        } catch (Exception ex) {
            throw BusinessException.of(CANCEL_TOUR_BOOKING_SERVICES_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> sendCheckingServiceAvailable(CheckingServiceAvailableDTO dto) {
        try {
            TourBookingService tourBookingService = tourBookingServiceRepository.findById(dto.getTourBookingServiceId()).orElseThrow();
            tourBookingService.setStatus(TourBookingServiceStatus.CHECKING);

            if(dto.getNewQuantity() > 0) {
                tourBookingService.setRequestedQuantity(dto.getNewQuantity());
                tourBookingService.setReason(dto.getReason());
                tourBookingService.setRequestDate(LocalDateTime.now());
            }


            TourBookingService updatedTourBookingService = tourBookingServiceRepository.save(tourBookingService);
            return GeneralResponse.of(bookingMapper.toTourBookingServiceDTO(updatedTourBookingService));
        } catch (Exception ex) {
            throw BusinessException.of(CANCEL_TOUR_BOOKING_SERVICES_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourPrivateByName(String name) {
        try {
            String normalizedName = removeAccents(name.toLowerCase());
            //List<Tour> tours = tourRepository.findByNameContainingAndTourType(normalizedName , TourType.PRIVATE);
            List<Tour> tours = tourRepository.findAll(bookingHelper.searchByNameAndTourType(normalizedName, TourType.PRIVATE));
            List<TourSupportInfoDTO> tourDTOs = tours.stream().map(bookingMapper::toTourSupportInfoDTO).toList();
            return GeneralResponse.of(tourDTOs);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_PRIVATE_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourContents(Long tourId) {
        try {
            Tour tour = tourRepository.findById(tourId).orElseThrow();
            TourContentSaleResponseDTO tourContentSaleResponseDTO = bookingMapper.toTourContentSaleResponseDTO(tour);
            tourContentSaleResponseDTO.setCreatedAt(tour.getCreatedAt());
            return GeneralResponse.of(tourContentSaleResponseDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_PRIVATE_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getLocations() {
        try {
            List<Location> locations = locationRepository.findByDeletedFalse();
            List<LocationShortDTO> locationDTOS = locations.stream().map(locationMapper::toLocationShortDTO).toList();
            return GeneralResponse.of(locationDTOS);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_PRIVATE_LIST_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> createTourPrivate(CreateTourPrivateRequestDTO tour) {

        try {
            Tour temp = tourRepository.findByName(tour.getName());
            if (temp != null) {
                throw BusinessException.of("Tên tour đã tồn tại");
            } else {

                List<Location> locations = tour.getLocations().stream()
                        .map(loc -> locationRepository.findById(loc.getId())
                                .orElseThrow(() -> new RuntimeException("Không timg thấy địa điểm")))
                        .toList();


                Tour newTour = Tour.builder()
                        .name(tour.getName())
                        .numberDays(tour.getNumberDays())
                        .numberNights(tour.getNumberNights())
                        .departLocation(Location.builder().id(tour.getDepartLocation()).build())
                        .highlights(tour.getHighlights())
                        .note(tour.getNote())
                        .tourType(TourType.PRIVATE)
                        .locations(locations)
                        .deleted(false)
                        .createdBy(User.builder().id(tour.getCreatedBy()).build())
                        .tourStatus(TourStatus.DRAFT)
                        .build();

                Tour savedTour = tourRepository.save(newTour);



                List<TourImage> tourImages = new ArrayList<>();
                for(String imageUrl : tour.getTourImages()) {
                    TourImage tourImage = TourImage.builder()
                            .imageUrl(imageUrl)
                            .tour(savedTour)
                            .deleted(false)
                            .build();
                    tourImages.add(tourImage);
                }

                tourImageRepository.saveAll(tourImages);


                TourPax tourPax = TourPax.builder()
                        .tour(savedTour)
                        .maxPax(tour.getPax())
                        .minPax(tour.getPax())
                        .deleted(false)
                        .validFrom(new Date())
                        .validTo(new Date())
                        .build();

                List<TourDay> tourDays = bookingHelper.generateTourDays(savedTour.getNumberDays(), savedTour);

                tourDayRepository.saveAll(tourDays);

                tourPaxRepository.save(tourPax);

                return GeneralResponse.of(tour);


            }

        } catch (Exception ex) {
            throw BusinessException.of(TOUR_CREATE_FAIL, ex);
        }

    }

    @Override
    @Transactional
    public GeneralResponse<?> updateTourPrivate(UpdateTourPrivateContentRequestDTO tour) {
        try {

            List<TourPax> tourPaxEntities = tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(tour.getTourId());

            TourPax tourPax = tourPaxEntities.get(0);

            Tour tourEntity = tourRepository.findById(tour.getTourId()).orElseThrow();

            tourEntity.setHighlights(tour.getHighlights());
            tourEntity.setNote(tour.getNotes());
            tourEntity.setPrivacy(tour.getPrivacy());

            Tour savedTour = tourRepository.save(tourEntity);

            TourSchedule tourSchedule = null;

            if (tour.getTourScheduleId() != null) {
                tourSchedule = tourScheduleRepository.findById(tour.getTourScheduleId()).orElseThrow();
                tourSchedule.setStartDate(tour.getStartDate());
                tourSchedule.setEndDate(tour.getEndDate());
            } else {
                tourSchedule = TourSchedule.builder()
                        .startDate(tour.getStartDate())
                        .endDate(tour.getEndDate())
                        .tour(savedTour)
                        .tourPax(tourPax)
                        .deleted(false)
                        .status(TourScheduleStatus.OPEN)
                        .build();
            }
            tourScheduleRepository.save(tourSchedule);

            List<Long> tourDayIds = tour.getTourDays().stream()
                    .map(TourDayPrivateRequestDTO::getId)
                    .toList();


            Set<Long> allServiceCategoryIds = tour.getTourDays().stream()
                    .flatMap(dto -> dto.getServiceCategoryIds().stream())
                    .collect(Collectors.toSet());

            Map<Long, ServiceCategory> serviceCategoryMap = serviceCategoryRepository.findAllById(allServiceCategoryIds)
                    .stream()
                    .collect(Collectors.toMap(ServiceCategory::getId, sc -> sc));


            List<TourDay> tourDays = tourDayRepository.findAllById(tourDayIds);

            Map<Long, TourDay> tourDayMap = tourDays.stream()
                    .collect(Collectors.toMap(TourDay::getId, td -> td));


            for (TourDayPrivateRequestDTO dto : tour.getTourDays()) {
                TourDay tourDay = tourDayMap.get(dto.getId());
                if (tourDay != null) {
                    tourDay.setTitle(dto.getTitle());
                    tourDay.setContent(dto.getContent());
                    tourDay.setLocation(Location.builder().id(dto.getLocationId()).build());
                    tourDay.setMealPlan(dto.getMeals());


                    List<TourDayServiceCategory> tourDayServiceCategories = new ArrayList<>();

                    for (Long serviceCategoryId : dto.getServiceCategoryIds()) {
                        ServiceCategory serviceCategory = serviceCategoryMap.get(serviceCategoryId);
                        if (serviceCategory != null) {
                            tourDayServiceCategories.add(
                                    TourDayServiceCategory.builder()
                                            .tourDay(tourDay)
                                            .serviceCategory(serviceCategory)
                                            .build()
                            );
                        }
                    }

                    tourDayServiceCategoryRepository.saveAll(tourDayServiceCategories);

                }
            }

            tourDayRepository.saveAll(tourDays);

            return GeneralResponse.of(bookingMapper.toTourDetailSaleResponseDTO(savedTour));

        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_TOUR_PRIVATE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> updateTourPrivateStatus(ChangeStatusTourPrivateRequestDTO tour) {
        try {
            Tour tourEntity = tourRepository.findById(tour.getId()).orElseThrow();
            tourEntity.setTourStatus(tour.getTourStatus());
            Tour savedTour = tourRepository.save(tourEntity);
            return GeneralResponse.of(bookingMapper.toTourDetailSaleResponseDTO(savedTour));
        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_TOUR_STATUS_FAIL, ex);
        }
    }

    @Override

    public GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>> viewListBookingHistory(int page, int size, String keyword, String paymentStatus, String orderDate) {
        try {
            Long currentId = getCurrentUserId();
            Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Specification<TourBooking> spec = buildSearchSpecification(keyword, paymentStatus)
                    .and((root, query, criteriaBuilder) -> {
                        Join<TourBooking, User> userJoin = root.join("user");
                        return criteriaBuilder.equal(userJoin.get("id"), currentId);
                    });

            Page<TourBooking> tourPage = tourBookingRepository.findAll(spec, pageable);

            // Map to DTO
            List<TourBookingHistoryDTO> resultDTO = tourPage.getContent().stream()
                    .map(booking -> {
                                return TourBookingHistoryDTO.builder()
                                        .bookingId(booking.getId())
                                        .bookingDate(booking.getCreatedAt())
                                        .bookingCode(booking.getBookingCode())
                                        .tourId(booking.getTour().getId())
                                        .tourName(booking.getTour().getName())
                                        .tourImage(booking.getTour().getTourImages().get(0).getImageUrl())
                                        .bookingStatus(booking.getStatus())
                                        .bookingTotalAmount(booking.getTotalAmount())
                                        .bookingExpiredAt(booking.getExpiredAt())
                                        .build();
                            }

                    )
                    .collect(Collectors.toList());

            return buildPagedResponse(tourPage, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_BOOKING_HISTORY_LIST_FAIL, ex);
        }
    }

    private Specification<TourBooking> buildSearchSpecification(String keyword, String paymentStatus) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            // Search by tour name
            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Ensure PostgreSQL has UNACCENT enabled
                Expression<String> normalizedTourName = cb.function("unaccent", String.class, cb.lower(root.join("tour", JoinType.LEFT).get("name")));

                // Remove accents from the input keyword
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                Predicate tourNamePredicate = cb.like(normalizedTourName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                // Combine both conditions
                predicates.add(tourNamePredicate);
            }


            // Filter by status
            if (paymentStatus != null) {
                predicates.add(cb.equal(root.get("status"), paymentStatus));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private <T> GeneralResponse<PagingDTO<List<T>>> buildPagedResponse(Page<TourBooking> tourPage, List<T> tours) {
        PagingDTO<List<T>> pagingDTO = PagingDTO.<List<T>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tours)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "ok", pagingDTO);
    }
    public GeneralResponse<?> getServiceCategoryWithTourDays(Long tourId) {
        try {
            List<Object[]> results = tourDayServiceCategoryRepository.findServiceCategoriesWithTourDaysByTourId(tourId);

            // Map ServiceCategory to its corresponding TourDays
            Map<Long, ServiceCategoryWithTourDayResponseDTO> categoryMap = new HashMap<>();

            for (Object[] row : results) {
                ServiceCategory serviceCategory = (ServiceCategory) row[0];
                TourDay tourDay = (TourDay) row[1];

                categoryMap.computeIfAbsent(serviceCategory.getId(), id ->
                        ServiceCategoryWithTourDayResponseDTO.builder()
                                .id(serviceCategory.getId())
                                .categoryName(serviceCategory.getCategoryName())
                                .tourDays(new ArrayList<>())
                                .build());

                categoryMap.get(serviceCategory.getId()).getTourDays()
                        .add(bookingMapper.toTourDayShortInfoDTO(tourDay));
            }

            for (ServiceCategoryWithTourDayResponseDTO dto : categoryMap.values()) {
                dto.setTourDays(dto.getTourDays().stream()
                        .distinct()
                        .collect(Collectors.toList()));
            }
            return GeneralResponse.of(new ArrayList<>(categoryMap.values()));
        } catch (Exception ex) {
            throw BusinessException.of("Lấy danh mục dịch vụ theo ngày tour thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourLocations(Long tourId) {
        try {
            Tour tourEntity = tourRepository.findById(tourId).orElseThrow();
            List<Location> locations = tourEntity.getLocations();
            List<LocationShortDTO> dto = locations.stream().map(locationMapper::toLocationShortDTO).toList();
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_LOCATIONS_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getServiceProviders(Long locationId, String categoryName) {
        try {
            List<ServiceProvider> providers;
            if(categoryName.equalsIgnoreCase("Flight Ticket")) {
                providers = serviceProviderRepository.getServiceByServiceCategory(categoryName);
            }  else {
                providers = serviceProviderRepository.getServiceByLocationIdAndServiceCategory(locationId, categoryName);
            }
            List<ServiceProviderSimpleDTO> dto = providers.stream().map(bookingMapper::toServiceProviderSimpleDTO).toList();
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(GET_PROVIDER_BY_LOCATION_FAIL , ex);
        }
    }

    @Override
    public GeneralResponse<?> getServiceProviderServices(Long providerId, String categoryName) {
        try {
            List<com.fpt.capstone.tourism.model.Service> services = serviceRepository.findByServiceCategoryNameAndProviderId(categoryName, providerId);
            List<AvailableServiceDTO> availableServices = buildAvailableServicesDTO(services);
            return GeneralResponse.of(availableServices);
        } catch (Exception ex) {
            throw BusinessException.of(GET_PROVIDER_SERVICES_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> updateTourServices(List<TourPrivateServiceRequestDTO> dto) {
        try {
            List<Long> tourDayIds = dto.stream()
                    .map(TourPrivateServiceRequestDTO::getId)
                    .toList();
            // Fetch existing TourDays in one query
            List<TourDay> tourDays = tourDayRepository.findAllById(tourDayIds);

            // Convert list to Map for fast lookup
            Map<Long, TourDay> tourDayMap = tourDays.stream()
                    .collect(Collectors.toMap(TourDay::getId, td -> td));

            // Process updates
            for (TourPrivateServiceRequestDTO dtoObject : dto) {
                TourDay tourDay = tourDayMap.get(dtoObject.getId());
                if (tourDay != null) {
                    updateTourDayServices(tourDay, dtoObject.getServices());
                }
            }

            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_SERVICES_FAIL, ex);
        }
    }

    @Override
    public void updateTourDayServices(TourDay tourDay, List<Long> serviceIds) {
        Set<Long> existingServiceIds = tourDay.getTourDayServices().stream()
                .map(tds -> tds.getService().getId())
                .collect(Collectors.toSet());

        // Prepare new services to be added
        List<TourDayService> newServices = new ArrayList<>();
        for (Long serviceId : serviceIds) {
            if (!existingServiceIds.contains(serviceId)) {
                com.fpt.capstone.tourism.model.Service service = serviceRepository.findById(serviceId)
                        .orElseThrow(() -> BusinessException.of(SERVICE_NOT_FOUND ));

                TourDayService newTourDayService = new TourDayService();
                newTourDayService.setTourDay(tourDay);
                newTourDayService.setService(service);
                newServices.add(newTourDayService);
            }
        }

        // Identify services that need to be removed
        List<TourDayService> servicesToRemove = tourDay.getTourDayServices().stream()
                .filter(tds -> !serviceIds.contains(tds.getService().getId()))
                .toList();

        // Remove from database explicitly
        if (!servicesToRemove.isEmpty()) {
            tourDayServiceRepository.deleteAll(servicesToRemove);
        }

        // Add new services
        if (!newServices.isEmpty()) {
            tourDayServiceRepository.saveAll(newServices);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> cancelBooking(CancelTourBookingRequestDTO dto) {
        try {
            TourBooking tourBookingEntity = tourBookingRepository.findByBookingId(dto.getBookingId());
            tourBookingEntity.setStatus(dto.getStatus());
            tourBookingEntity.setReason(dto.getReason());
            tourBookingRepository.save(tourBookingEntity);


            //Hủy khách hàng trong đoàn
            List<TourBookingCustomer> customers = tourBookingEntity.getCustomers();
            for (TourBookingCustomer customer : customers) {
                customer.setDeleted(true);
            }

            tourBookingCustomerRepository.saveAll(customers);


            //Hủy hóa đơn
            List<Transaction> transactions = transactionRepository.findAllByBookingAndCategoryIn(tourBookingEntity, List.of(TransactionType.RECEIPT));
            for (Transaction transaction : transactions) {
                if(!transaction.getTransactionStatus().toString().equalsIgnoreCase(TransactionStatus.PAID.toString())) {
                    transaction.setTransactionStatus(TransactionStatus.CANCELLED);
                }

                List<CostAccount> costAccounts = costAccountRepository.findByTransaction_Id(transaction.getId());
                for (CostAccount costAccount : costAccounts) {
                    if(!costAccount.getStatus().toString().equalsIgnoreCase(CostAccountStatus.PAID.toString())) {
                        costAccount.setStatus(CostAccountStatus.CANCELLED);
                    }
                }
                costAccountRepository.saveAll(costAccounts);

            }

            transactionRepository.saveAll(transactions);

            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_LOCATIONS_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> sendPricing(Long tourId) {
        try {
            Tour tourEntity = tourRepository.findById(tourId).orElseThrow();
            tourEntity.setTourStatus(TourStatus.PENDING_PRICING);
            tourRepository.save(tourEntity);
            return GeneralResponse.of(tourId);
        } catch (Exception ex) {
            throw BusinessException.of(SEND_PRICING_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> updateBookingStatus(BookingStatusUpdateDTO dto) {
        try {
            TourBooking tourBooking = tourBookingRepository.findByBookingId(dto.getId());
            tourBooking.setStatus(dto.getBookingStatus());
            tourBookingRepository.save(tourBooking);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_BOOKING_STATUS_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> sendOperator(SendOperatorDTO dto) {
        try {
            TourSchedule tourSchedule = tourScheduleRepository.findById(dto.getTourScheduleId()).orElseThrow();
            tourSchedule.setStatus(TourScheduleStatus.ONGOING);
            tourScheduleRepository.save(tourSchedule);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(SEND_OPERATOR_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> takeBooking(TakeBookingRequestDTO dto) {
        try {
            TourBooking tourBooking = tourBookingRepository.findByBookingId(dto.getBookingId());
            tourBooking.setSale(User.builder().id(dto.getSaleId()).build());
            tourBookingRepository.save(tourBooking);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(TAKE_BOOKING_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> changePaymentMethod(Long id, PaymentMethod paymentMethod) {
        try {
            TourBooking tourBooking = tourBookingRepository.findByBookingId(id);
            tourBooking.setPaymentMethod(paymentMethod);
            tourBookingRepository.save(tourBooking);
            return GeneralResponse.of(paymentMethod);
        } catch (Exception ex) {
            throw BusinessException.of("Sửa phương thức thanh toán thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> cancelBooking(String bookingCode) {
        try {
            TourBooking tourBooking = tourBookingRepository.findByBookingCode(bookingCode);

            if(!tourBooking.getStatus().equals(TourBookingStatus.PENDING)){
                throw BusinessException.of("Bạn không có quyền hủy booking này");
            }
            tourBooking.setStatus(TourBookingStatus.CANCELLED);
            tourBookingRepository.save(tourBooking);
            return new GeneralResponse<>(HttpStatus.OK.value(), "Hủy đơn thành công", bookingCode);
        } catch (Exception ex) {
            throw BusinessException.of(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> createCustomer(SaleCreateUserRequestDTO dto) {
        try {

            if (userService.existsByUsername(dto.getUsername())) {
                throw BusinessException.of(HttpStatus.CONFLICT, USERNAME_ALREADY_EXISTS_MESSAGE);
            }
            if (userService.exitsByEmail(dto.getEmail())) {
                throw BusinessException.of(HttpStatus.CONFLICT, EMAIL_ALREADY_EXISTS_MESSAGE);
            }

            User entity = userFullInformationMapper.toUser(dto);
            entity.setDeleted(false);
            User savedUser = userRepository.save(entity);
            UserRole userRole = UserRole.builder()
                    .role(Role.builder().id(1L).build())
                    .user(savedUser)
                    .deleted(false)
                    .build();
            userRoleRepository.save(userRole);
            return GeneralResponse.of(bookingMapper.toBookedPersonDTO(savedUser));
        } catch (Exception ex) {
            throw BusinessException.of(ex.getMessage(), ex);
        }
    }

    @Override
    public GeneralResponse<?> getForwardSchedule(ForwardScheduleRequestDTO dto) {
        try {
            List<PublicTourScheduleDTO> response = tourScheduleRepository.findTourScheduleBasicByTourIdAndNotEqualScheduleId(dto.getTourId(), dto.getScheduleId(), dto.getSeats());
            return GeneralResponse.of(response);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> forwardBooking(ForwardBookingRequestDTO dto) {
        try {
            TourBooking tourBooking = tourBookingRepository.findByBookingId(dto.getBookingId());
            tourBooking.setTourSchedule(TourSchedule.builder().id(dto.getScheduleId()).build());
            tourBookingRepository.save(tourBooking);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> checkingAllService(Long bookingId) {
        try {
            List<TourBookingService> tourBookingServices = tourBookingServiceRepository.findByTourBookingId(bookingId);
            for(TourBookingService tbs : tourBookingServices) {
                if(tbs.getStatus().toString().equalsIgnoreCase(TourBookingServiceStatus.NOT_ORDERED.toString())) {
                    tbs.setStatus(TourBookingServiceStatus.CHECKING);
                }
            }
            tourBookingServiceRepository.saveAll(tourBookingServices);
            return GeneralResponse.of(bookingId);
        } catch (Exception ex) {
            throw BusinessException.of(CHECKING_ALL_SERVICE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getEmailContent(SendPriceRequestDTO dto) {
        try {
            Tour tourEntity = tourRepository.findById(dto.getTourId()).orElseThrow();
            TourSchedule tourSchedule = tourScheduleRepository.findById(dto.getScheduleId()).orElseThrow();
            TourPax tourPax = tourPaxRepository.findById(tourSchedule.getTourPax().getId()).orElseThrow();

            String htmlTemplate = bookingHelper.loadTemplate(Constants.FilePath.PRICE_EMAIL_PATH);


            htmlTemplate = htmlTemplate.replace("{{tourName}}", tourEntity.getName())
                    .replace("{{numberDays}}", String.valueOf(tourEntity.getNumberDays()))
                    .replace("{{numberNights}}", String.valueOf(tourEntity.getNumberNights()))
                    .replace("{{departLocation}}", tourEntity.getDepartLocation().getName())
                    .replace("{{highlights}}", tourEntity.getHighlights())
                    .replace("{{privacy}}", tourEntity.getPrivacy())
                    .replace("{{adultPrice}}", String.format("%,.0f", tourPax.getSellingPrice()))
                    .replace("{{childPrice}}", String.format("%,.0f", tourPax.getSellingPrice() * 0.75))
                    .replace("{{locations}}", tourEntity.getLocations().stream().map(Location::getName).collect(Collectors.joining(", ")))
                    .replace("{{tags}}", tourEntity.getTags().stream().map(Tag::getName).collect(Collectors.joining(", ")));

            List<TourDay> sortedTourDays = tourEntity.getTourDays()
                    .stream()
                    .sorted(Comparator.comparing(TourDay::getDayNumber))
                    .toList();

            StringBuilder dayHtml = new StringBuilder();
            for (TourDay day : sortedTourDays) {
                dayHtml.append("<div class=\"day\">")
                        .append("<strong>Ngày ").append(day.getDayNumber()).append(":</strong> ").append(day.getTitle()).append("<br/>")
                        .append("<em>").append(day.getContent()).append("</em>")
                        .append("</div>");
            }


            htmlTemplate = htmlTemplate.replace("{{tourDays}}", dayHtml.toString());

            return GeneralResponse.of(htmlTemplate);
        } catch (Exception ex) {
            throw BusinessException.of("Tạo email báo giá thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> sendEmailPrice(SendEmailPriceRequestDTO dto) {
        try {
            emailService.sendEmailHtml(dto.getEmail(), dto.getSubject(), dto.getContent());

            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Gửi email báo giá thất bại", ex);
        }
    }

    @Override
    public void confirmPayment(int paymentStatus, String orderInfo) {
        try {
            PaymentResponseDTO dto = PaymentResponseDTO.builder()
                    .bookingCode(orderInfo)
                    .build();
            if(paymentStatus == 1) {
                TourBooking tourBooking = tourBookingRepository.findByBookingCode(orderInfo);
                tourBooking.setStatus(TourBookingStatus.SUCCESS);
                tourBookingRepository.save(tourBooking);

                List<Transaction> transactions = transactionRepository.findByBooking_Id(tourBooking.getId());

                for(Transaction transaction : transactions) {
                    transaction.setTransactionStatus(TransactionStatus.PAID);
                    List<CostAccount> costAccounts = transaction.getCostAccount();
                    for(CostAccount costAccount : costAccounts) {
                        costAccount.setStatus(CostAccountStatus.PAID);
                    }

                    costAccountRepository.saveAll(costAccounts);
                }

                transactionRepository.saveAll(transactions);

                dto.setPaymentStatus("SUCCESS");

            } else {
                dto.setPaymentStatus("FAILED");
            }
            GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Thanh toán booking thất bại", ex);
        }
    }


    @Override
    public GeneralResponse<?> getAllRefundRequest(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<TourBooking> spec = buildSearchSpecification(keyword, isDeleted)
                    .and((root, query, criteriaBuilder) -> {
                        return criteriaBuilder.equal(root.get("status"), TourBookingStatus.REQUEST_CANCELLED_WITH_REFUND);
                    });

            Page<TourBooking> tourBookingPage = tourBookingRepository.findAll(spec, pageable);

            return bookingHelper.buildPagedResponse(tourBookingPage);
        } catch (Exception ex) {
            throw BusinessException.of(GET_DATA_FAIL, ex);
        }
    }

    public Specification<TourBooking> buildSearchSpecification(String keyword, Boolean isDeleted) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                // Search in booking code
                Expression<String> normalizedBookingCode = cb.function("unaccent", String.class, cb.lower(root.get("bookingCode")));
                Predicate bookingCodePredicate = cb.like(normalizedBookingCode, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                // Search in tour name
                Join<TourBooking, Tour> tourJoin = root.join("tour", JoinType.LEFT);
                Expression<String> normalizedTourName = cb.function("unaccent", String.class, cb.lower(tourJoin.get("name")));
                Predicate tourNamePredicate = cb.like(normalizedTourName, cb.concat("%", cb.concat(normalizedKeyword, "%")));



                predicates.add(cb.or(bookingCodePredicate, tourNamePredicate));
            }

            //TourBookingStatus
            predicates.add(cb.or(
                    cb.equal(root.get("status"), TourBookingStatus.REQUEST_CANCELLED_WITH_REFUND)
            ));

            // Filter by status
            if (keyword != null) {
                try {
                    TourBookingStatus status = TourBookingStatus.valueOf(keyword.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status values
                }
            }

            // Filter by deletion status
            if (isDeleted != null) {
                predicates.add(cb.equal(root.get("deleted"), isDeleted));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public GeneralResponse<?> getDetailRefundRequest(Long tourBookingId) {
        try {
            List<RefundDetailDTO> refundDetailDTO = tourBookingRepository.findDetailRefundRequestByBookingId(tourBookingId, TourBookingStatus.REQUEST_CANCELLED_WITH_REFUND);
            return GeneralResponse.of(refundDetailDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_DATA_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> approveRefundRequest(Long tourBookingId) {
        try {
            TourBooking tourBooking = tourBookingRepository.findById(tourBookingId).orElseThrow(
                    () -> BusinessException.of("Không tìm thấy booking")
            );
            if(!tourBooking.getStatus().equals(TourBookingStatus.REQUEST_CANCELLED_WITH_REFUND)){
                throw BusinessException.of("Không có quyền duyệt");
            }
            tourBooking.setStatus(TourBookingStatus.CANCELLED_WITH_REFUND);
            tourBookingRepository.save(tourBooking);
            return GeneralResponse.of(tourBookingId);
        } catch (Exception ex) {
            throw BusinessException.of(APPROVE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> rejectRefundRequest(Long tourBookingId) {
        try {
            TourBooking tourBooking = tourBookingRepository.findById(tourBookingId).orElseThrow(
                    () -> BusinessException.of("Không tìm thấy booking")
            );
            if(!tourBooking.getStatus().equals(TourBookingStatus.REQUEST_CANCELLED_WITH_REFUND)){
                throw BusinessException.of("Không có quyền duyệt");
            }
            tourBooking.setStatus(TourBookingStatus.CANCELLED_WITHOUT_REFUND);
            tourBookingRepository.save(tourBooking);
            return GeneralResponse.of(tourBookingId);
        } catch (Exception ex) {
            throw BusinessException.of(REJECT_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> successService(Long tourBookingServiceId) {
        try {
            TourBookingService tourBookingService = tourBookingServiceRepository.findById(tourBookingServiceId).orElseThrow();
            tourBookingService.setStatus(TourBookingServiceStatus.AVAILABLE);
            tourBookingService.setRequestedQuantity(0);
            TourBookingService updatedTourBookingService = tourBookingServiceRepository.save(tourBookingService);
            return GeneralResponse.of(bookingMapper.toTourBookingServiceDTO(updatedTourBookingService));
        } catch (Exception ex) {
            throw BusinessException.of(CANCEL_TOUR_BOOKING_SERVICES_FAIL, ex);
        }
    }

    private final RoomRepository roomRepository;
    private final MealRepository mealRepository;

    private List<AvailableServiceDTO> buildAvailableServicesDTO(List<com.fpt.capstone.tourism.model.Service> services) {
        List<AvailableServiceDTO> availableServices = new ArrayList<>();

        for (com.fpt.capstone.tourism.model.Service service : services) {
            String status = determineServiceStatus(service.getStartDate(), service.getEndDate());
            String categoryName = service.getServiceCategory() != null ? service.getServiceCategory().getCategoryName() : null;

            // Get type-specific details based on service category
            RoomDetailDTO roomDetail = null;
            MealDetailDTO mealDetail = null;
            TransportDetailDTO transportDetail = null;

            if (HOTEL.equalsIgnoreCase(categoryName)) {
                Optional<Room> roomOpt = roomRepository.findByServiceIdAndDeletedFalse(service.getId());
                if (roomOpt.isPresent()) {
                    Room room = roomOpt.get();
                    roomDetail = RoomDetailDTO.builder()
                            .id(room.getId())
                            .capacity(room.getCapacity())
                            .availableQuantity(room.getAvailableQuantity())
                            .facilities(room.getFacilities())
                            .build();
                }
            } else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
                Optional<Meal> mealOpt = mealRepository.findByServiceIdAndDeletedFalse(service.getId());
                if (mealOpt.isPresent()) {
                    Meal meal = mealOpt.get();
                    mealDetail = MealDetailDTO.builder()
                            .id(meal.getId())
                            .type(meal.getType().name())
                            .mealDetail(meal.getMealDetail())
                            .build();
                }
            }

            AvailableServiceDTO serviceDTO = AvailableServiceDTO.builder()
                    .id(service.getId())
                    .name(service.getName())
                    .categoryName(categoryName)
                    .nettPrice(service.getNettPrice())
                    .sellingPrice(service.getSellingPrice())
                    .status(status)
                    .startDate(service.getStartDate())
                    .endDate(service.getEndDate())
                    .providerId(service.getServiceProvider() != null ? service.getServiceProvider().getId() : null)
                    .providerName(service.getServiceProvider() != null ? service.getServiceProvider().getName() : null)
                    .roomDetail(roomDetail)
                    .mealDetail(mealDetail)
                    .build();

            availableServices.add(serviceDTO);
        }

        return availableServices;
    }

    private String determineServiceStatus(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime now = LocalDateTime.now();
        if (startDateTime == null || endDateTime == null) {
            return "UNKNOWN";
        }
        if (now.isBefore(startDateTime)) {
            return "UPCOMING";
        } else if (now.isAfter(endDateTime)) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }

    public static String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);//Chuyển chữ có dấu thành ký tự gốc + dấu (ví dụ: Đà → Da + dấu huyền).
        Pattern pattern = Pattern.compile("\\p{M}"); //  Xóa tất cả các dấu khỏi ký tự.
        return pattern.matcher(normalized).replaceAll("");
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> BusinessException.of(USER_NOT_FOUND));
            return user.getId();
        }
        throw BusinessException.of("Không tìm thấy thông tin người dùng");
    }
}
