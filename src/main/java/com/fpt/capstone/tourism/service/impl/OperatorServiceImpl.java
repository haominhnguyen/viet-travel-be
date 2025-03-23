package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.AddServiceRequestDTO;
import com.fpt.capstone.tourism.dto.request.AssignTourGuideRequestDTO;
import com.fpt.capstone.tourism.dto.request.PayServiceRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourOperationLogRequestDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.EmailConfirmationService;
import com.fpt.capstone.tourism.service.OperatorService;
import jakarta.persistence.*;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@org.springframework.stereotype.Service
public class OperatorServiceImpl implements OperatorService {
    private final TourScheduleRepository tourScheduleRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourBookingRepository tourBookingRepository;
    private final TourBookingCustomerRepository tourBookingCustomerRepository;
    private final TourOperationLogRepository logRepository;
    private final TransactionRepository transactionRepository;
    private final TourScheduleServiceRepository scheduleServiceRepository;
    private final CostAccountRepository costAccountRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceProviderRepository providerRepository;
    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;
    private final MealRepository mealRepository;
    private final TransportRepository transportRepository;
    private final TourBookingServiceRepository bookingServiceRepository;
    private final TourBookingCustomerFullMapper customerFullMapper;
    private final TourOperationLogMapper logMapper;
    private final TransactionMapper transactionMapper;
    private final TagMapper tagMapper;
    private final UserFullInformationMapper userMapper;
    private final ServiceProviderMapper providerMapper;
    private final ServiceMapper serviceMapper;
    private final RoomMapper roomMapper;
    private final MealMapper mealMapper;
    private final TransportMapper transportMapper;
    private final EmailConfirmationService emailService;

    @Override
    public GeneralResponse<PagingDTO<List<OperatorTourDTO>>> getListTour(int page, int size, String keyword, String status, String orderDate) {
        try {
            Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Specification<TourSchedule> spec = buildSearchSpecification(keyword, status);

            Page<TourSchedule> tourPage = tourScheduleRepository.findAll(spec, pageable);


            List<Long> scheduleIds = tourPage.getContent().stream()
                    .map(TourSchedule::getId)
                    .collect(Collectors.toList());

            Map<Long, Integer> availableSeatsMap = tourScheduleRepository.findAvailableSeatsByScheduleIds(scheduleIds)
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],  // scheduleId
                            row -> (Integer) row[1] // availableSeats
                    ));


            // Map to DTO
            List<OperatorTourDTO> operatorTourDTOS = tourPage.getContent().stream()
                    .map(tourSchedule -> new OperatorTourDTO(
                            tourSchedule.getId(),
                            tourSchedule.getStartDate(),
                            tourSchedule.getEndDate(),
                            tourSchedule.getStatus().toString(),
                            tourSchedule.getTour().getName(),
                            Optional.ofNullable(tourSchedule.getTourGuide()).map(User::getFullName).orElse(null),
                            Optional.ofNullable(tourSchedule.getOperator()).map(User::getFullName).orElse(null),
                            tourSchedule.getTourPax().getMaxPax(),
                            availableSeatsMap.getOrDefault(tourSchedule.getId(), 0)
                    ))
                    .collect(Collectors.toList());

            return buildPagedResponse(tourPage, operatorTourDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("Operator get all tour fail", ex);
        }
    }

    @Override
    public GeneralResponse<OperatorTourDTO> operateTour(Long id) {
        try {

            TourSchedule tourSchedule = tourScheduleRepository.findById(id).orElseThrow();

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            User user = userRepository.findByUsername(username).orElseThrow(() ->
                    BusinessException.of("User not found"));

            tourSchedule.setOperator(user);
            tourScheduleRepository.save(tourSchedule);

            Map<Long, Integer> availableSeatsMap = tourScheduleRepository
                    .findAvailableSeatsByScheduleIds(Collections.singletonList(tourSchedule.getId()))
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],  // scheduleId
                            row -> (Integer) row[1] // availableSeats
                    ));

            OperatorTourDTO operatorTourDTO = OperatorTourDTO.builder()
                    .scheduleId(tourSchedule.getId())
                    .startDate(tourSchedule.getStartDate())
                    .endDate(tourSchedule.getEndDate())
                    .status(tourSchedule.getStatus().toString())
                    .tourName(tourSchedule.getTour().getName())
                    .tourGuide(Optional.ofNullable(tourSchedule.getTourGuide()).map(User::getFullName).orElse(null))
                    .operator(user.getFullName())
                    .maxPax(tourSchedule.getTourPax().getMaxPax())
                    .availableSeats(availableSeatsMap.getOrDefault(tourSchedule.getId(), 0))
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Operator received tour to operate successfully", operatorTourDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Operator receive tour fail", ex);
        }
    }

    @Override
    public GeneralResponse<OperatorTourDetailDTO> getTourDetail(Long scheduleId) {
        try {
            TourSchedule tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow(() -> BusinessException.of("Tour schedule not found"));

            Tour tour = tourRepository.findByScheduleId(scheduleId);

            Map<Long, Integer> availableSeatsMap = tourScheduleRepository.findAvailableSeatsByScheduleIds(Collections.singletonList(scheduleId))
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],  // scheduleId
                            row -> ((Number) row[1]).intValue()  // soldSeats
                    ));

            //tìm số tiền đã chi trong tour (đã chi + tạm ứng)
            Double paidMoney = Optional.ofNullable(
                    tourScheduleRepository.findPaidTourCostByScheduleId(scheduleId)
            ).orElse(0.0);

            //tìm doanh thu của tour (đã thu + thu hộ)
            Double revenueMoney = Optional.ofNullable(
                    tourScheduleRepository.findRevenueCostByScheduleId(scheduleId)
            ).orElse(0.0);

            //tìm số tiền còn lại của tour (doanh thu - đã chi)
            Double remainMoney = revenueMoney - paidMoney;

            OperatorTourDetailDTO operatorTourDetailDTO = OperatorTourDetailDTO.builder()
                    .scheduleId(scheduleId)
                    .tourName(tour.getName())
                    .tourType(tour.getTourType())
                    .tags(tagMapper.toDtoList(tour.getTags()))
                    .numberDays(tour.getNumberDays())
                    .numberNights(tour.getNumberNights())
                    .departureLocation(tour.getDepartLocation().getName())
                    .startDate(tourSchedule.getStartDate())
                    .endDate(tourSchedule.getEndDate())
                    .createdAt(tour.getCreatedAt())
                    .createdBy(tour.getCreatedBy().getFullName())
                    .maxPax(tourSchedule.getTourPax().getMaxPax())
                    .soldSeats(tourScheduleRepository.findSoldSeatsByScheduleId(scheduleId))
                    .pendingSeats(tourScheduleRepository.findPendingSeatsByScheduleId(scheduleId))
                    .remainingSeats(availableSeatsMap.getOrDefault(scheduleId, 0))
                    .operatorName(Optional.ofNullable(tourSchedule.getOperator()).map(User::getFullName).orElse("null"))
                    .departureTime(tourSchedule.getDepartureTime() != null ? tourSchedule.getDepartureTime() : null)
                    .tourGuideName(Optional.ofNullable(tourSchedule.getTourGuide()).map(User::getFullName).orElse("null"))
                    .meetingLocation(tourSchedule.getMeetingLocation() != null ? tourSchedule.getMeetingLocation() : "null")
                    .totalTourCost(tourScheduleRepository.findTotalTourCostByScheduleId(scheduleId))
                    .paidTourCost(paidMoney)
                    .remainingTourCost(remainMoney)
                    .revenueCost(revenueMoney)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), "Operator get tour detail successfully", operatorTourDetailDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Operator get tour detail fail", ex);
        }
    }

    @Override
    public GeneralResponse<List<OperatorTourCustomerDTO>> getListCustomerOfTourDetail(Long scheduleId) {
        try {
            List<TourBooking> bookings = tourBookingRepository.findByTourSchedule_Id(scheduleId);

            List<OperatorTourCustomerDTO> responseList = bookings.stream().map(booking -> {
                List<TourBookingCustomerDTO> customers = tourBookingCustomerRepository
                        .findByTourBookingId(booking.getId())
                        .stream()
                        .map(customerFullMapper::toDto)
                        .collect(Collectors.toList());

                OperatorTourCustomerDTO responseDTO = OperatorTourCustomerDTO.builder()
                        .tourBookingId(booking.getId())
                        .tourBookingCategory(booking.getTourBookingCategory())
                        .listCustomer(customers)
                        .build();
                return responseDTO;
            }).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), "Operator get list customer of tour detail success", responseList);
        } catch (Exception ex) {
            throw BusinessException.of("Operator get list customer of tour detail fail", ex);
        }
    }

    @Override
    public GeneralResponse<List<OperatorTourBookingDTO>> getListBookingOfTourDetail(Long scheduleId) {
        try {
            List<TourBooking> bookings = tourBookingRepository.findByTourSchedule_Id(scheduleId);

            List<OperatorTourBookingDTO> responseList = bookings.stream().map(booking -> {

                Integer adultCount = tourBookingRepository.countAdultNumberByBookingId(booking.getId());
                Integer childCount = tourBookingRepository.countChildNumberByBookingId(booking.getId());

                //Số tiền đã thu
                Double receiptAmount = tourBookingRepository.findReceiptAmountByBookingId(booking.getId());
                //Số tiền HDV đã thu hộ
                Double collectionAmount = tourBookingRepository.findCollectionAmountByBookingId(booking.getId());

                OperatorTourBookingDTO responseDTO = OperatorTourBookingDTO.builder()
                        .bookingId(booking.getId())
                        .bookingCode(booking.getBookingCode())
                        .bookedBy(booking.getUser().getFullName())
                        .adultCount(adultCount)
                        .childCount(childCount)
                        .customerCount(adultCount + childCount)
                        .bookingCategory(booking.getTourBookingCategory())
                        .receiptAmount(receiptAmount)
                        .remainingAmount(booking.getTotalAmount() - receiptAmount)
                        .collectionAmount(collectionAmount)
                        .totalAmount(booking.getTotalAmount())
                        .bookedAt(booking.getCreatedAt())
                        .bookingStatus(booking.getStatus())
                        .build();
                return responseDTO;
            }).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), "Operator get list booking of tour detail success", responseList);
        } catch (Exception ex) {
            throw BusinessException.of("Operator get list booking of tour detail fail", ex);
        }
    }

    @Override
    public GeneralResponse<List<TourOperationLogDTO>> getListOperationLogOfTourDetail(Long scheduleId) {
        try {
            List<TourOperationLog> logs = logRepository.findByTourSchedule_IdAndDeletedFalse(scheduleId);

            List<TourOperationLogDTO> responseList = logs.stream()
                    .map(logMapper::toDTO).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list log of tour detail success", responseList);
        } catch (Exception ex) {
            throw BusinessException.of("Get list log of tour detail fail", ex);
        }
    }

    @Override
    public GeneralResponse<TourOperationLogDTO> createOperationLog(Long scheduleId, TourOperationLogRequestDTO logRequestDTO) {
        try {
            //Validate input data
            Validator.validateLog(logRequestDTO);
            TourSchedule tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow(() ->
                    BusinessException.of("Not found tour schedule"));

            //Save date to database
            TourOperationLog log = logMapper.toEntity(logRequestDTO);
            log.setCreatedAt(LocalDateTime.now());
            log.setDeleted(false);
            log.setTourSchedule(tourSchedule);
            logRepository.save(log);

            TourOperationLogDTO logDTO = logMapper.toDTO(log);

            return new GeneralResponse<>(HttpStatus.OK.value(), "Create log success", logDTO);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of("Create log fail", ex);
        }
    }

    @Override
    public GeneralResponse<TourOperationLogDTO> deleteOperationLog(Long logId) {
        try {
            TourOperationLog log = logRepository.findById(logId).orElseThrow(() ->
                    BusinessException.of("Not found tour log"));

            log.setDeleted(true);
            log.setUpdatedAt(LocalDateTime.now());
            logRepository.save(log);

            TourOperationLogDTO logDTO = logMapper.toDTO(log);
            return new GeneralResponse<>(HttpStatus.OK.value(), "Delete log success", logDTO);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of("Delete log fail", ex);
        }
    }

    @Override
    public GeneralResponse<AssignTourGuideRequestDTO> assignTourGuide(Long scheduleId, AssignTourGuideRequestDTO requestDTO) {
        try {
            TourSchedule tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow(
                    () -> BusinessException.of("Not found tour schedule"));

            //Find tour guide
            User tourGuide = userRepository.findById(requestDTO.getTourGuideId()).orElseThrow(
                    () -> BusinessException.of("Not found tour guide"));

            //Update
            tourSchedule.setMeetingLocation(requestDTO.getMeetingLocation());
            tourSchedule.setDepartureTime(requestDTO.getDepartureTime());
            tourSchedule.setTourGuide(tourGuide);

            //Save to database
            tourScheduleRepository.save(tourSchedule);

            return new GeneralResponse<>(HttpStatus.OK.value(), "Assign tour guide success", requestDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Assign tour guide fail", ex);
        }
    }

    @Override
    public GeneralResponse<List<UserResponseDTO>> getListAvailableTourGuide(Long scheduleId) {
        try {
            List<UserResponseDTO> responseList = userRepository.findAvailableTourGuideByScheduleId(scheduleId).stream()
                    .map(userMapper::toResponseDTO).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list available tour guide success", responseList);
        } catch (Exception ex) {
            throw BusinessException.of("Get list available tour guide fail", ex);
        }
    }

    @Override
    public GeneralResponse<List<OperatorTransactionDTO>> getListTransaction(Long scheduleId) {
        try {
            List<TourBooking> tourBookings = tourBookingRepository.findByTourSchedule_Id(scheduleId);
            List<Transaction> transactions = transactionRepository.findAllByBookingIn(tourBookings);

            List<OperatorTransactionDTO> responseList = transactions.stream().map(transactionMapper::toDTO)
                    .collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list transaction success", responseList);
        } catch (Exception ex) {
            throw BusinessException.of("Get list transaction fail", ex);
        }
    }

    @Override
    public GeneralResponse<OperatorServiceListDTO> getListService(Long scheduleId) {
        try {
            // Tìm danh sách tất cả dịch vụ liên quan đến scheduleId
            List<TourBooking> bookings = tourBookingRepository.findByTourSchedule_Id(scheduleId);
            List<TourBookingService> bookingServices = scheduleServiceRepository.findAllByBookingIn(bookings);
            // Danh sách DTO kết quả
            List<OperatorServiceDTO> serviceDTOList = new ArrayList<>();

            // Tổng hợp số tiền
            double totalPaid = 0.0; // Tổng số tiền đã trả cho nhà cung cấp
            double totalAmountToPay = 0.0; // Tổng số tiền cần trả cho nhà cung cấp

            for (TourBookingService bookingService : bookingServices) {
                // Tìm danh sách Transaction có category = PAYMENT
//                    List<Transaction> transactions = transactionRepository.findByBooking_Id(bookingService.getBooking().getId())
//                            .stream()
//                            .filter(transaction -> transaction.getCategory() == TransactionType.PAYMENT)
//                            .collect(Collectors.toList());
//
//                    // Tính tổng số tiền đã chi cho nahf cung cấp theo dịch vụ và booking
//                    double paidForBooking = transactions.stream()
//                            .flatMap(transaction -> costAccountRepository.findByTransaction_Id(transaction.getId()).stream())
//                            .filter(costAccount -> costAccount.getStatus() == CostAccountStatus.PAID)
//                            .mapToDouble(CostAccount::getFinalAmount) // Tính tổng số tiền đã chi
//                            .sum();
//
                // Tính tổng số tiền đã chi cho nahf cung cấp theo dịch vụ và booking
                double paidForBooking = transactionRepository.getTotalPaidForBooking(bookingService.getBooking().getId());

                // Tính tổng số tiền phải trả cho nhà cung cấp theo booking
                double amountToPayForBooking = bookingService.getCurrentQuantity() * bookingService.getService().getNettPrice();

                // Cập nhật tổng tiền đã trả & tổng số tiền cần trả
                totalPaid += paidForBooking;
                totalAmountToPay += amountToPayForBooking;

                // Xác định trạng thái thanh toán của booking
                String paymentStatus;
                if (paidForBooking >= amountToPayForBooking) {
                    paymentStatus = "PAID"; // Đã thanh toán đủ
                } else if (paidForBooking > 0) {
                    paymentStatus = "PARTIALLY_PAID"; // Thanh toán một phần
                } else {
                    paymentStatus = "UNPAID"; // Chưa thanh toán
                }

                // Thêm vào danh sách DTO
                serviceDTOList.add(OperatorServiceDTO.builder()
                        .bookingId(bookingService.getBooking().getId())
                        .serviceId(bookingService.getService().getId())
                        .bookingCode(bookingService.getBooking().getBookingCode())
                        .serviceName(bookingService.getService().getName())
                        .serviceCategory(bookingService.getService().getServiceCategory().getCategoryName())
                        .usingDate(bookingService.getRequestDate())
                        .requestQuantity(bookingService.getRequestedQuantity())
                        .currentQuantity(bookingService.getCurrentQuantity())
                        .bookingStatus(bookingService.getStatus().toString())
                        .paidForBooking(paidForBooking)
                        .amountToPayForBooking(amountToPayForBooking)
                        .paymentStatus(paymentStatus) // Trả về trạng thái của từng booking
                        .build());
            }

            // Tạo DTO tổng hợp kết quả
            OperatorServiceListDTO resultDTO = OperatorServiceListDTO.builder()
                    .services(serviceDTOList) // Danh sách dịch vụ theo booking
                    .totalNumOfService((int) serviceDTOList.stream().map(OperatorServiceDTO::getServiceId).count()) // Đếm số lượng dịch vụ
                    .paidAmount(totalPaid) // Tổng số tiền đã trả
                    .remainingAmount(totalAmountToPay - totalPaid) // Số tiền còn lại phải trả
                    .totalAmount(totalAmountToPay) // Tổng số tiền phải trả
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list service success", resultDTO);

        } catch (Exception ex) {
            throw BusinessException.of("Get list service fail", ex);
        }
    }

    @Override
    public GeneralResponse<PublicServiceProviderDTO> chooseServiceToPay(Long serviceId) {
        try {
            Service service = serviceRepository.findById(serviceId).orElseThrow(
                    () -> BusinessException.of("Service not found"));
            ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
                    () -> BusinessException.of("Service provider not found")
            );
            PublicServiceProviderDTO resultDTO = providerMapper.toPublicServiceProviderDTO(serviceProvider);
            return new GeneralResponse<>(HttpStatus.OK.value(), "Choose service success", resultDTO);

        } catch (Exception ex) {
            throw BusinessException.of("Choose service fail", ex);
        }
    }

    @Transactional
    @Override
    public GeneralResponse<OperatorTransactionDTO> payService(PayServiceRequestDTO requestDTO) {
        try {
            TourBooking tourBooking = tourBookingRepository.findById(requestDTO.getBookingId()).orElseThrow(
                    () -> BusinessException.of("Booking not found")
            );

            Transaction transaction = Transaction.builder()
                    .booking(tourBooking)
                    .amount(requestDTO.getAmount())
                    .category(TransactionType.PAYMENT)
                    .paidBy(requestDTO.getPaidBy())
                    .receivedBy(requestDTO.getReceivedBy())
                    .paymentMethod(requestDTO.getPaymentMethod())
                    .notes(requestDTO.getNotes())
                    .build();

            Transaction transaction1 = transactionRepository.save(transaction);

            Service service = serviceRepository.findById(requestDTO.getServiceId()).orElseThrow(
                    () -> BusinessException.of("Service not found")
            );
            List<CostAccount> costAccounts = new ArrayList<>();
            costAccounts.add(CostAccount.builder()
                    .transaction(transaction1)
                    .amount(service.getNettPrice())
                    .discount(0)
                    .content(requestDTO.getNotes())
                    .quantity(requestDTO.getQuantity())
                    .finalAmount(service.getNettPrice() * requestDTO.getQuantity())
                    .status(CostAccountStatus.PENDING)
                    .build());

            List<CostAccount> newList = costAccountRepository.saveAll(costAccounts);
            transaction1.setCostAccount(newList);

            OperatorTransactionDTO resultDTO = transactionMapper.toDTO(transaction1);
            return new GeneralResponse<>(HttpStatus.OK.value(), "Pay service success", resultDTO);

        } catch (Exception ex) {
            throw BusinessException.of("Pay service fail", ex);
        }
    }

    @Override
    public GeneralResponse<Map<Long, String>> getListLocation() {
        try {
            List<Location> locations = locationRepository.findByDeletedFalse();
            Map<Long, String> resultDTO = locations.stream()
                    .collect(Collectors.toMap(Location::getId, Location::getName));
            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list location success", resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Get list location fail", ex);
        }
    }

    @Override
    public GeneralResponse<Map<Long, String>> getListServiceProviderByLocationId(Long locationId) {
        try {
            List<ServiceProvider> providers = providerRepository.findByLocationIdAndDeletedFalse(locationId);
            Map<Long, String> resultDTO = providers.stream()
                    .collect(Collectors.toMap(ServiceProvider::getId, ServiceProvider::getName));
            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list provider by location success", resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Get list provider by location fail", ex);
        }
    }

    @Override
    public GeneralResponse<List<ServiceSimpleDTO>> getListServiceByServiceProviderId(Long serviceProviderId) {
        try {
            List<Service> services = serviceRepository.findByServiceProviderIdAndDeletedFalse(serviceProviderId);
            List<ServiceSimpleDTO> resultDTO = services.stream()
                    .map(serviceMapper::toSimpleDTO)
                    .collect(Collectors.toList());
            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list service by provider success", resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Get list service by provider fail", ex);
        }
    }

    @Override
    public GeneralResponse<?> getServiceDetail(Long serviceId) {
        try {
            Service service = serviceRepository.findById(serviceId).orElseThrow(
                    () -> BusinessException.of("Service not found")
            );
            RoomSimpleDTO roomDTO = roomRepository.findByServiceId(serviceId)
                    .map(roomMapper::toSimpleDTO).orElse(null);
            MealSimpleDTO mealDTO = mealRepository.findByServiceId(serviceId)
                    .map(mealMapper::toSimpleDTO).orElse(null);
            TransportSimpleDTO transportDTO = transportRepository.findByServiceId(serviceId)
                    .map(transportMapper::toSimpleDTO).orElse(null);


            OperatorServiceDetailDTO resultDTO = OperatorServiceDetailDTO.builder()
                    .id(serviceId)
                    .name(service.getName())
                    .nettPrice(service.getNettPrice())
                    .sellingPrice(service.getSellingPrice())
                    .imageUrl(service.getImageUrl())
                    .startDate(service.getStartDate())
                    .endDate(service.getEndDate())
                    .serviceCategory(service.getServiceCategory().getCategoryName())
                    .serviceProvider(service.getServiceProvider().getName())
                    .room(roomDTO)
                    .meal(mealDTO)
                    .transport(transportDTO)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), "Success", resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Fail", ex);
        }
    }

    @Override
    public GeneralResponse<?> addService(AddServiceRequestDTO requestDTO) {
        try {
            Service service = serviceRepository.findById(requestDTO.getServiceId()).orElseThrow(
                    () -> BusinessException.of("Service not found")
            );
            TourBooking booking = tourBookingRepository.findById(requestDTO.getBookingId()).orElseThrow(
                    () -> BusinessException.of("Tour booking not found")
            );

            TourBookingService bookingService = bookingServiceRepository.findByBookingIdAndServiceIdAndDeletedFalse(requestDTO.getBookingId(), requestDTO.getServiceId());

            //Dịch vụ chưa được đặt => update số lượng
            if (bookingService != null && bookingService.getStatus().equals(TourBookingServiceStatus.NOT_ORDERED)) {
                bookingService.setCurrentQuantity(bookingService.getCurrentQuantity() + requestDTO.getAddQuantity());
                bookingService.setRequestDate(requestDTO.getRequestDate());
                bookingServiceRepository.save(bookingService);
            } else {
                //dịch vụ chưa có thì add vào db
                if (bookingService == null) {
                    bookingService = TourBookingService.builder()
                            .booking(booking)
                            .service(service)
                            .currentQuantity(requestDTO.getAddQuantity())
                            .requestDate(requestDTO.getRequestDate())
                            .deleted(Boolean.FALSE)
                            .reason(requestDTO.getReason())
                            .status(TourBookingServiceStatus.PENDING)
                            .build();
                    bookingServiceRepository.save(bookingService);
                }
                if (bookingService.getStatus().equals(TourBookingServiceStatus.APPROVED)) {
                    bookingService.setCurrentQuantity(bookingService.getCurrentQuantity() + requestDTO.getAddQuantity());
                    bookingService.setStatus(TourBookingServiceStatus.PENDING);
                    bookingServiceRepository.save(bookingService);
                }

                ServiceProvider provider = service.getServiceProvider();

                String emailSubject = "[Viet Travel - " + provider.getId() + "] - Thông tin đặt hàng dịch vụ.";
                String emailContent = "Kính gửi: " + provider.getName() + ",\n\n"
                        + "Dưới đây là thông tin đặt dịch vụ của chúng tôi. Mong quý đối tác vui lòng sắp xếp và xác nhận thông tin sau:\n\n"
                        + "Dịch vụ: " + service.getName() + "\n"
                        + "Số lượng: " + requestDTO.getAddQuantity() + "\n"
                        + "Ngày yêu cầu: " + requestDTO.getRequestDate() + ".\n\n"
                        + "Tổng số tiền: " + requestDTO.getAddQuantity() * service.getNettPrice() + "(đ)\n\n"
                        + "Vui lòng cho chúng tôi biết phản hồi trong thời gian sớm nhất.\n\n"
                        + "Best Regards,\n"
                        + "Viet Travel";
                MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
                        .providerId(provider.getId())
                        .providerName(provider.getName())
                        .providerEmail(provider.getEmail())
                        .emailSubject(emailSubject)
                        .emailContent(emailContent)
                        .build();

                return new GeneralResponse<>(HttpStatus.OK.value(), "Need confirm", mailServiceDTO);

            }

            return new GeneralResponse<>(HttpStatus.OK.value(), "Success", "Update quantity successfully");
        } catch (Exception ex) {
            throw BusinessException.of("Fail", ex);
        }
    }

    @Override
    public GeneralResponse<?> sendMailToProvider(MailServiceDTO mailServiceDTO) {
        try {
            emailService.sendMailServiceProvider(mailServiceDTO);
            return new GeneralResponse<>(HttpStatus.OK.value(), "Success", mailServiceDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Fail", ex);
        }
    }

    @Override
    public GeneralResponse<?> getListChangeServiceRequest() {
        try {
//            List<TourBookingService> bookingServices = bookingServiceRepository.findByBookingIdAndServiceIdAndDeletedFalse()
//            emailService.sendMailServiceProvider(mailServiceDTO);
            return new GeneralResponse<>(HttpStatus.OK.value(), "Success", "mailServiceDTO");
        } catch (Exception ex) {
            throw BusinessException.of("Fail", ex);
        }
    }


    private Specification<TourSchedule> buildSearchSpecification(String keyword, String status) {
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
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private <T> GeneralResponse<PagingDTO<List<T>>> buildPagedResponse(Page<TourSchedule> tourPage, List<T> tours) {
        PagingDTO<List<T>> pagingDTO = PagingDTO.<List<T>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tours)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "ok", pagingDTO);
    }
}
