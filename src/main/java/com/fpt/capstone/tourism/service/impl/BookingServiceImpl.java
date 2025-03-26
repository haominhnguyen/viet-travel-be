package com.fpt.capstone.tourism.service.impl;

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
import com.fpt.capstone.tourism.service.TourBookingCustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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


    private final LocationMapper locationMapper;
    private final TourImageMapper tourImageMapper;
    private final TourBookingCustomerMapper tourBookingCustomerMapper;


    private final BookingHelper bookingHelper;
    private final BookingMapper bookingMapper;
    private final TourHelper tourHelper;

    private final TourBookingCustomerService tourBookingCustomerService;
    private final CostAccountRepository costAccountRepository;
    private final LocationRepository locationRepository;
    private final TourPaxRepository tourPaxRepository;

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
    @Transactional
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

            saveTourBookingService(result);

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
    @Transactional
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

            saveTourBookingService(result);

            //Create transaction for booking
            createReceiptBookingTransaction(result, bookingRequestDTO.getTotalAmount(), bookingRequestDTO.getFullName(), bookingRequestDTO.getPaymentMethod());

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
            log.info("Start find tour booking detail with ID: {}", bookingId);
            TourBooking tourBooking = tourBookingRepository.findByBookingId(bookingId);
            log.info("End find tour booking detail with ID: {}", bookingId);

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

            int modifiedQuantity = tourBookingCustomer.getDeleted() ? -1 : 1;

            TourBooking tourBooking = tourBookingRepository.findById(tourBookingCustomer.getTourBooking().getId()).orElseThrow();
            tourBooking.setSeats(tourBooking.getSeats() + modifiedQuantity);
            tourBookingRepository.save(tourBooking);

            return GeneralResponse.of(bookingMapper.toTourBookingCustomerDTO(updatedTourBookingCustomer));
        } catch (Exception ex) {
            throw BusinessException.of("Update Customer Status failed", ex);
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
                if(!customer.getDeleted()) {
                    totalCustomer++;
                }
            }

            tourBooking.setSeats(totalCustomer);

            tourBookingRepository.save(tourBooking);

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
            TourInfoInCreateBookingDTO tourDetailSaleResponseDTO = bookingMapper.toCreateBookingTourDTO(tour);
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
    public GeneralResponse<?> updateTourBookingService(Long tourBookingServiceID) {
        return null;
    }

    @Override
    public Transaction createReceiptBookingTransaction(TourBooking tourBooking, Double total, String fullName, PaymentMethod paymentMethod) {

        Transaction transaction = Transaction.builder()
                .booking(tourBooking)
                .amount(total)
                .paymentMethod(paymentMethod)
                .category(TransactionType.RECEIPT)
                .paidBy(fullName)
                .transactionStatus(TransactionStatus.MISSING)
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

        List<TourBookingCustomer> customers = tourBookingCustomerRepository.findByBookedPersonAndTourBooking(false, tourBooking);

        int totalRooms = calculateTotalRooms(customers);

        for(TourDay tourDay : tourDays) {
            List<TourDayService> dayServices = tourDay.getTourDayServices();
            for(TourDayService dayService : dayServices) {
                TourBookingService tourBookingService = TourBookingService.builder()
                        .booking(tourBooking)
                        .tourDay(tourDay)
                        .service(dayService.getService())
                        .deleted(false)
                        .status(TourBookingServiceStatus.SUCCESS)
                        .build();

                com.fpt.capstone.tourism.model.Service service = serviceRepository.findById(dayService.getService().getId()).orElseThrow();

                if(service.getServiceCategory().getCategoryName().equals("Hotel")) {
                    tourBookingService.setCurrentQuantity(totalRooms);
                } else if(service.getServiceCategory().getCategoryName().equals("Restaurant")) {
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
            TourBooking tourBooking = tourBookingRepository.findById(tourBookingID).orElseThrow();
            Tour tour = tourBooking.getTour();
            List<TourDay> tourDays = tourDayRepository.findAllByTourId(tour.getId());
            return GeneralResponse.of(bookingHelper.getTourBookingListService(tourDays, tourBooking));
        } catch (Exception ex) {
            throw BusinessException.of("Get tour booking services for sale failed", ex);
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
            throw BusinessException.of("Get tour booking services for sale failed", ex);
        }
    }

    @Override
    public GeneralResponse<?> cancelService(Long tourBookingServiceId) {
        try {
            TourBookingService tourBookingService = tourBookingServiceRepository.findById(tourBookingServiceId).orElseThrow();
            tourBookingService.setStatus(TourBookingServiceStatus.CANCELLED);
            TourBookingService updatedTourBookingService = tourBookingServiceRepository.save(tourBookingService);
            return GeneralResponse.of(bookingMapper.toTourBookingServiceDTO(updatedTourBookingService));
        } catch (Exception ex) {
            throw BusinessException.of("Cancel tour booking services for sale failed", ex);
        }
    }

    @Override
    public GeneralResponse<?> sendCheckingServiceAvailable(Long tourBookingServiceId) {
        try {
            TourBookingService tourBookingService = tourBookingServiceRepository.findById(tourBookingServiceId).orElseThrow();
            tourBookingService.setStatus(TourBookingServiceStatus.CHECKING);
            TourBookingService updatedTourBookingService = tourBookingServiceRepository.save(tourBookingService);
            return GeneralResponse.of(bookingMapper.toTourBookingServiceDTO(updatedTourBookingService));
        } catch (Exception ex) {
            throw BusinessException.of("Cancel tour booking services for sale failed", ex);
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
            throw BusinessException.of("Cannot get tour private list", ex);
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
            throw BusinessException.of("Cannot get tour private list", ex);
        }
    }

    @Override
    public GeneralResponse<?> getLocations() {
        try {
            List<Location> locations = locationRepository.findByDeletedFalse();
            List<LocationShortDTO> locationDTOS = locations.stream().map(locationMapper::toLocationShortDTO).toList();
            return GeneralResponse.of(locationDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("Cannot get tour private list", ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> createTourPrivate(CreateTourPrivateRequestDTO tour) {

        try {
            Tour temp = tourRepository.findByName(tour.getName());
            if(temp != null) {
                throw BusinessException.of("Tên tour đã tồn tại");
            } else {

                List<Location> locations = tour.getLocations().stream()
                        .map(loc -> locationRepository.findById(loc.getId())
                                .orElseThrow(() -> new RuntimeException("Location not found")))
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


                TourPax tourPax = TourPax.builder()
                        .tour(savedTour)
                        .maxPax(tour.getPax())
                        .minPax(tour.getPax())
                        .build();

                List<TourDay> tourDays = bookingHelper.generateTourDays(savedTour.getNumberDays(),savedTour);

                tourDayRepository.saveAll(tourDays);

                tourPaxRepository.save(tourPax);

                return GeneralResponse.of(tour);


            }

        } catch (Exception ex) {
            throw BusinessException.of("Create Tour Failed", ex);
        }

    }

    @Override
    public GeneralResponse<?> updateTourPrivate(UpdateTourPrivateContentRequestDTO tour) {
        try {

            TourSchedule tourSchedule = TourSchedule.builder().build();

        } catch (Exception ex) {
            throw BusinessException.of("Cannot get tour private list", ex);
        }

        return null;
    }

    public static String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);//Chuyển chữ có dấu thành ký tự gốc + dấu (ví dụ: Đà → Da + dấu huyền).
        Pattern pattern = Pattern.compile("\\p{M}"); //  Xóa tất cả các dấu khỏi ký tự.
        return pattern.matcher(normalized).replaceAll("");
    }
}
