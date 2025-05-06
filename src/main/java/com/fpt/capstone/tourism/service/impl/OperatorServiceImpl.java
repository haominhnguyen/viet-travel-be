package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.TransactionHelper;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.enums.*;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.EmailConfirmationService;
import com.fpt.capstone.tourism.service.OperatorService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

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
    private final ServicePaxPricingRepository servicePaxPricingRepository;
    private final CostAccountRepository costAccountRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceProviderRepository providerRepository;
    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;
    private final MealRepository mealRepository;
    private final TransportRepository transportRepository;
    private final TourBookingServiceRepository bookingServiceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final TourDayServiceRepository tourDayServiceRepository;
    private final TourDayRepository tourDayRepository;
    private final TourPaxRepository tourPaxRepository;
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
    private final TourBookingServiceMapper bookingServiceMapper;
    private final TourDayMapper tourDayMapper;
    private final EmailConfirmationService emailService;
    private final TransactionHelper transactionHelper;


    final String emailOrderServiceContent = "Kính gửi: {0},\n\n"
            + "Dưới đây là thông tin đặt dịch vụ của chúng tôi. Mong quý đối tác vui lòng sắp xếp và xác nhận thông tin sau:\n\n"
            + "Dịch vụ: {1}\n"
            + "Số lượng: {2}\n"
            + "Ngày yêu cầu: {3}.\n\n"
            + "Tổng số tiền: {4,number,#,###.##} (đ)\n\n"
            + "Kính mong quý đối tác cho chúng tôi biết phản hồi trong thời gian sớm nhất.\n\n"
            + "Best Regards,\n"
            + "Viet Travel";
    final String emailOrderServiceSubject = "[Viet Travel - {0}] - Thông tin đặt hàng dịch vụ.";
    final String emailChangeServiceContent = "Kính gửi: {0},\n\n"
            + "Chúng tôi xin thông báo về sự thay đổi số lượng dịch vụ đã đặt với quý đối tác như sau:\n\n"
            + "Dịch vụ: {1}\n"
            + "Số lượng cũ: {2}\n"
            + "Số lượng mới: {3}\n"
            + "Ngày yêu cầu: {4}.\n\n"
            + "Tổng số tiền mới: {5,number,#,###.##} (đ)\n\n"
            + "Lưu ý: Đây chỉ là thông báo về sự thay đổi số lượng dịch vụ. Nếu có bất kỳ vấn đề gì, vui lòng phản hồi trong thời gian sớm nhất.\n\n"
            + "Trân trọng,\n"
            + "Viet Travel";
    final String emailChangeServiceSubject = "[Viet Travel - {0}] - Thông báo thay đổi số lượng dịch vụ.";

    final String emailUpdateServiceContent = "Kính gửi: {0},\n\n"
            + "Chúng tôi xin thông báo về việc thay đổi số lượng dịch vụ đã đặt trước đó. Vui lòng xem thông tin chi tiết dưới đây và xác nhận lại khả năng đáp ứng:\n\n"
            + "**Dịch vụ:** {1}\n"
            + "**Số lượng ban đầu đã xác nhận:** {2}\n"
            + "**Số lượng mới yêu cầu:** {3}\n"
            + "**Ngày yêu cầu:** {4}.\n\n"
            + "**Lưu ý:** Dịch vụ với số lượng **{2}** đã được xác nhận trước đó. Chúng tôi cần xác nhận từ quý đối tác về việc có thể đáp ứng dịch vụ thay đổi hay không.\n\n"
            + "Tổng số tiền điều chỉnh (dự kiến): {5,number,#,###.##} (đ)\n\n"
            + "Kính mong quý đối tác phản hồi lại trong thời gian sớm nhất để chúng tôi có thể cập nhật thông tin đặt hàng.\n\n"
            + "Trân trọng,\n"
            + "**Viet Travel**";
    final String emailUpdateServiceSubject = "[Viet Travel - {0}] - Yêu cầu xác nhận thay đổi số lượng dịch vụ.";

    final String emailCancelServiceContent = "Kính gửi: {0},\n\n"
            + "Chúng tôi xin thông báo về việc hủy dịch vụ đã đặt với quý đối tác như sau:\n\n"
            + "Dịch vụ: {1}\n"
            + "Số lượng: {2}\n"
            + "Ngày yêu cầu: {3}.\n\n"
            + "Tổng số tiền đã đặt: {4,number,#,###.##} (đ)\n"
            + "Mọi vấn đề về hoàn tiền có liên quan sẽ được thực hiện theo thỏa thuận hợp đồng đã ký.\n\n"
            + "Lưu ý: Nếu có bất kỳ thắc mắc hoặc vấn đề gì, vui lòng phản hồi trong thời gian sớm nhất.\n\n"
            + "Trân trọng,\n"
            + "Viet Travel";

    final String emailRejectServiceUpdateSubject = "[Viet Travel - Yêu cầu thay đổi số lượng dịch vụ bị từ chối";

    final String emailRejectServiceUpdateContent = "Kính gửi: {0},\n\n"
            + "Chúng tôi xin thông báo rằng yêu cầu thay đổi số lượng dịch vụ của bạn đã bị nhà điều hành từ chối. " +
            "Dưới đây là thông tin chi tiết về yêu cầu:\n\n"
            + "Dịch vụ: {1}\n"
            + "Số lượng yêu cầu thay đổi: {2} → {3}\n"
            + "Ngày yêu cầu: {4}\n\n"
            + "Vui lòng kiểm tra lại yêu cầu và điều chỉnh phù hợp." +
            " Nếu có bất kỳ thắc mắc nào, bạn có thể liên hệ với bộ phận điều hành để được hỗ trợ thêm.\n\n"
            + "Trân trọng,\n"
            + "Viet Travel";

    final String emailCancelServiceSubject = "[Viet Travel - {0}] - Thông báo hủy dịch vụ.";


    @Override
    public GeneralResponse<PagingDTO<List<OperatorTourDTO>>> getListTour(int page, int size, String keyword, String status, String orderDate) {
        try {
            Long currentOperatorId = getCurrentUserOperatorId();
            Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Specification<TourSchedule> spec = buildSearchSpecification(keyword, status, TourType.SIC)
                    .and((root, query, criteriaBuilder) -> {
                        Join<TourSchedule, User> userJoin = root.join("operator");
                        return criteriaBuilder.equal(userJoin.get("id"), currentOperatorId);
                    });

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
            throw BusinessException.of(OPERATOR_GET_ALL_TOUR_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<OperatorTourDTO> operateTour(Long id) {
        try {

            TourSchedule tourSchedule = tourScheduleRepository.findById(id).orElseThrow();

            if (tourSchedule.getOperator() != null) {
                throw BusinessException.of("Đã có người điều hành lịch tour này");
            }

//            if (!tourSchedule.getStatus().equals(TourScheduleStatus.ONGOING)) {
//                throw BusinessException.of(("Lịch tour này chưa thể nhận điều hành"));
//            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            User user = userRepository.findByUsername(username).orElseThrow(() ->
                    BusinessException.of("Không tìm thấy người dùng"));

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
            return new GeneralResponse<>(HttpStatus.OK.value(), OPERATOR_RECEIVED_TOUR_SUCCESS, operatorTourDTO);
        } catch (Exception ex) {
            throw BusinessException.of(OPERATOR_RECEIVE_TOUR_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<OperatorTourDetailDTO> getTourDetail(Long scheduleId) {
        try {
            checkAuthor(scheduleId);

            TourSchedule tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow(() -> BusinessException.of("Không tìm thấy lịch trình tour"));

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
                    .scheduleStatus(tourSchedule.getStatus())
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
                    .minPax(tourSchedule.getTourPax().getMinPax())
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

            return new GeneralResponse<>(HttpStatus.OK.value(), OPERATOR_GET_TOUR_DETAIL_SUCCESS, operatorTourDetailDTO);
        } catch (Exception ex) {
            throw BusinessException.of(OPERATOR_GET_TOUR_DETAIL_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<List<OperatorTourCustomerDTO>> getListCustomerOfTourDetail(Long scheduleId) {
        try {
            checkAuthor(scheduleId);

            List<TourBooking> bookings = tourBookingRepository.findBookingByStatusAndTourSchedule_Id(TourBookingStatus.SUCCESS, scheduleId);

            List<OperatorTourCustomerDTO> responseList = bookings.stream().map(booking -> {
                List<TourBookingCustomerDTO> customers = tourBookingCustomerRepository
                        .findByTourBooking_IdAndBookedPersonFalse(booking.getId())
                        .stream()
                        .map(customerFullMapper::toDto)
                        .collect(Collectors.toList());

                OperatorTourCustomerDTO responseDTO = OperatorTourCustomerDTO.builder()
                        .tourBookingId(booking.getId())
                        .tourBookingCode(booking.getBookingCode())
                        .tourBookingCategory(booking.getTourBookingCategory())
                        .listCustomer(customers)
                        .build();
                return responseDTO;
            }).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), OPERATOR_GET_CUSTOMER_LIST_SUCCESS, responseList);
        } catch (Exception ex) {
            throw BusinessException.of(OPERATOR_GET_CUSTOMER_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<List<OperatorTourBookingDTO>> getListBookingOfTourDetail(Long scheduleId) {
        try {
            checkAuthor(scheduleId);

            List<TourBooking> bookings = tourBookingRepository.findByTourSchedule_Id(scheduleId);

            List<OperatorTourBookingDTO> responseList = bookings.stream().map(booking -> {

                Integer adultCount = tourBookingRepository.countAdultNumberByBookingId(booking.getId());
                Integer childCount = tourBookingRepository.countChildNumberByBookingId(booking.getId());

                //Số tiền đã thu
                Double receiptAmount = tourBookingRepository.findReceiptAmountByBookingId(booking.getId());
                //Số tiền HDV đã thu hộ
                Double collectionAmount = tourBookingRepository.findCollectionAmountByBookingId(booking.getId());

                String saleName = null;
                if (booking.getSale() != null) {
                    saleName = booking.getSale().getFullName();
                }

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
                        .saleName(saleName)
                        .build();
                return responseDTO;
            }).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), OPERATOR_GET_BOOKING_LIST_SUCCESS, responseList);
        } catch (Exception ex) {
            throw BusinessException.of(OPERATOR_GET_BOOKING_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<List<TourOperationLogDTO>> getListOperationLogOfTourDetail(Long scheduleId) {
        try {
            checkAuthor(scheduleId);
            List<TourOperationLog> logs = logRepository.findByTourSchedule_IdAndDeletedFalse(scheduleId);

            List<TourOperationLogDTO> responseList = logs.stream()
                    .map(logMapper::toDTO).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), GET_TOUR_LOG_LIST_SUCCESS, responseList);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_LOG_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<TourOperationLogDTO> createOperationLog(Long scheduleId, TourOperationLogRequestDTO logRequestDTO) {
        try {
            checkAuthor(scheduleId);
            //Validate input data
            Validator.validateLog(logRequestDTO);
            TourSchedule tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow(() ->
                    BusinessException.of(TOUR_SCHEDULE_NOT_FOUND));

            //Save date to database
            TourOperationLog log = logMapper.toEntity(logRequestDTO);
            log.setCreatedAt(LocalDateTime.now());
            log.setDeleted(false);
            log.setTourSchedule(tourSchedule);
            logRepository.save(log);

            TourOperationLogDTO logDTO = logMapper.toDTO(log);

            return new GeneralResponse<>(HttpStatus.OK.value(), CREATE_LOG_SUCCESS, logDTO);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(CREATE_LOG_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<TourOperationLogDTO> deleteOperationLog(Long logId) {
        try {
            TourOperationLog log = logRepository.findById(logId).orElseThrow(() ->
                    BusinessException.of(TOUR_LOG_NOT_FOUND));

            checkAuthor(log.getTourSchedule().getId());
            log.setDeleted(true);
            log.setUpdatedAt(LocalDateTime.now());
            logRepository.save(log);

            TourOperationLogDTO logDTO = logMapper.toDTO(log);
            return new GeneralResponse<>(HttpStatus.OK.value(), DELETE_LOG_SUCCESS, logDTO);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(DELETE_LOG_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<AssignTourGuideRequestDTO> assignTourGuide(Long scheduleId, AssignTourGuideRequestDTO requestDTO) {
        try {
            checkAuthor(scheduleId);
            TourSchedule tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow(
                    () -> BusinessException.of(TOUR_SCHEDULE_NOT_FOUND));

            //Find tour guide
            User tourGuide = userRepository.findById(requestDTO.getTourGuideId()).orElseThrow(
                    () -> BusinessException.of(TOUR_GUIDE_NOT_FOUND));

            //Update
            tourSchedule.setMeetingLocation(requestDTO.getMeetingLocation());
            tourSchedule.setDepartureTime(requestDTO.getDepartureTime());
            tourSchedule.setTourGuide(tourGuide);

            //Save to database
            tourScheduleRepository.save(tourSchedule);

            return new GeneralResponse<>(HttpStatus.OK.value(), ASSIGN_TOUR_GUIDE_SUCCESS, requestDTO);
        } catch (Exception ex) {
            throw BusinessException.of(ASSIGN_TOUR_GUIDE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<List<UserResponseDTO>> getListAvailableTourGuide(Long scheduleId) {
        try {
            List<UserResponseDTO> responseList = userRepository.findAvailableTourGuideByScheduleId(scheduleId).stream()
                    .map(userMapper::toResponseDTO).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), GET_AVAILABLE_TOUR_GUIDE_SUCCESS, responseList);
        } catch (Exception ex) {
            throw BusinessException.of(GET_AVAILABLE_TOUR_GUIDE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<List<OperatorTransactionDTO>> getListTransaction(Long scheduleId) {
        try {
            checkAuthor(scheduleId);
            List<TourBooking> tourBookings = tourBookingRepository.findByTourSchedule_Id(scheduleId);
            List<Transaction> transactions = transactionRepository.findAllByBookingIn(tourBookings);

            List<OperatorTransactionDTO> responseList = transactions.stream().map(transactionMapper::toDTO)
                    .collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), GET_TRANSACTION_LIST_SUCCESS, responseList);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TRANSACTION_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<OperatorServiceListDTO> getListService(Long scheduleId) {
        try {
            checkAuthor(scheduleId);
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
//                List<Transaction> transactions = transactionRepository.findByBooking_Id(bookingService.getBooking().getId())
//                        .stream()
//                        .filter(transaction -> transaction.getCategory() == TransactionType.PAYMENT)
//                        .collect(Collectors.toList());
//
//                // Tính tổng số tiền đã chi cho nahf cung cấp theo dịch vụ và booking
//                double paidForBooking = transactions.stream()
//                        .flatMap(transaction -> costAccountRepository.findByTransaction_Id(transaction.getId()).stream())
//                        .filter(costAccount -> costAccount.getStatus() == CostAccountStatus.PAID)
//                        .mapToDouble(CostAccount::getFinalAmount) // Tính tổng số tiền đã chi
//                        .sum();
//
                // Tính tổng số tiền đã chi cho nahf cung cấp theo dịch vụ và booking
//            double paidForBooking = transactionRepository.getTotalPaidForBooking(bookingService.getId(), bookingService.getService().getId());
                //Kiểm tra có phải là dịch vụ biến đổi không (ví dụ xe đi chung)
                String serviceCategory = serviceRepository.findCategoryById(bookingService.getService().getId());
                double paidForBooking = 0;
                if (bookingService.getStatus().equals(TourBookingServiceStatus.PAID)) {
                    if (serviceCategory.equals("Transport")) {
                        paidForBooking = transactionHelper.calculateTransportFeePerPerson(scheduleId, bookingService.getService().getId());
                    } else {
                        paidForBooking = bookingService.getCurrentQuantity() * bookingService.getService().getNettPrice();
                    }
                }

                // Tính tổng số tiền phải trả cho nhà cung cấp theo booking
                double amountToPayForBooking = 0;
                if (!(bookingService.getStatus().equals(TourBookingServiceStatus.REJECTED)
                        || bookingService.getStatus().equals(TourBookingServiceStatus.NOT_AVAILABLE)
                        || bookingService.getStatus().equals(TourBookingServiceStatus.REJECTED_BY_OPERATOR)
                        || bookingService.getStatus().equals(TourBookingServiceStatus.CANCELLED))) {
                    if (serviceCategory.equals("Transport")) {
                        amountToPayForBooking = transactionHelper.calculateTransportFeePerPerson(scheduleId, bookingService.getService().getId());
                    } else {
                        amountToPayForBooking = bookingService.getCurrentQuantity() * bookingService.getService().getNettPrice();
                    }
                }


                // Cập nhật tổng tiền đã trả & tổng số tiền cần trả
                totalPaid += paidForBooking;
                totalAmountToPay += amountToPayForBooking;

//            // Xác định trạng thái thanh toán của booking
//            String paymentStatus;
//            if (paidForBooking >= amountToPayForBooking) {
//                paymentStatus = "PAID"; // Đã thanh toán đủ
//            } else if (paidForBooking > 0) {
//                paymentStatus = "PARTIALLY_PAID"; // Thanh toán một phần
//            } else {
//                paymentStatus = "UNPAID"; // Chưa thanh toán
//            }

                // Thêm vào danh sách DTO
                Service service = bookingService.getService();
                serviceDTOList.add(OperatorServiceDTO.builder()
                        .bookingServiceId(bookingService.getId())
                        .bookingId(bookingService.getBooking().getId())
                        .serviceId(service.getId())
                        .providerName(service.getServiceProvider().getName())
                        .providerEmail(service.getServiceProvider().getEmail())
                        .location(service.getServiceProvider().getLocation().getName())
                        .bookingCode(bookingService.getBooking().getBookingCode())
                        .serviceName(service.getName())
                        .serviceCategory(service.getServiceCategory().getCategoryName())
                        .usingDate(bookingService.getRequestDate())
                        .requestQuantity(bookingService.getRequestedQuantity())
                        .currentQuantity(bookingService.getCurrentQuantity())
                        .bookingStatus(bookingService.getStatus().toString())
                        .paidForBooking(paidForBooking)
                        .amountToPayForBooking(amountToPayForBooking)
                        .tourDayId(Optional.ofNullable(bookingService.getTourDay().getId()).orElseThrow(null))
//                    .paymentStatus(paymentStatus) // Trả về trạng thái của từng booking
                        .build());
            }
            serviceDTOList.sort(Comparator.comparing(
                    OperatorServiceDTO::getBookingStatus,
                    Comparator.nullsLast(Comparator.naturalOrder()))
            );

            // Tạo DTO tổng hợp kết quả
            OperatorServiceListDTO resultDTO = OperatorServiceListDTO.builder()
                    .services(serviceDTOList) // Danh sách dịch vụ theo booking
                    .totalNumOfService((int) serviceDTOList.stream().map(OperatorServiceDTO::getServiceId).count()) // Đếm số lượng dịch vụ
                    .paidAmount(totalPaid) // Tổng số tiền đã trả
                    .remainingAmount(totalAmountToPay - totalPaid) // Số tiền còn lại phải trả
                    .totalAmount(totalAmountToPay) // Tổng số tiền phải trả
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), GET_SERVICE_LIST_SUCCESS, resultDTO);

        } catch (Exception ex) {
            throw BusinessException.of(GET_SERVICE_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<PublicServiceProviderDTO> chooseServiceToPay(Long serviceId) {
        try {
            Service service = serviceRepository.findById(serviceId).orElseThrow(
                    () -> BusinessException.of(SERVICE_NOT_FOUND));
            ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
                    () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
            );
            PublicServiceProviderDTO resultDTO = providerMapper.toPublicServiceProviderDTO(serviceProvider);
            return new GeneralResponse<>(HttpStatus.OK.value(), CHOOSE_SERVICE_SUCCESS, resultDTO);

        } catch (Exception ex) {
            throw BusinessException.of(CHOOSE_SERVICE_FAIL, ex);
        }
    }

    @Transactional
    @Override
    public GeneralResponse<OperatorTransactionDTO> payService(PayServiceRequestDTO requestDTO) {
        try {
            TourBooking tourBooking = tourBookingRepository.findById(requestDTO.getBookingId()).orElseThrow(
                    () -> BusinessException.of(BOOKING_NOT_FOUND)
            );

            List<TourBookingService> tourBookingService =
                    bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(
                            requestDTO.getBookingId(), requestDTO.getServiceId(), requestDTO.getTourDayId()
                    );

            List<TourBookingService> bookingServiceNeedToPay = tourBookingService.stream().filter(tourBookingService1 ->
                    tourBookingService1.getStatus().equals(TourBookingServiceStatus.AVAILABLE)).toList();

            if (bookingServiceNeedToPay.isEmpty()) {
                throw BusinessException.of(SERVICE_REQUEST_NOT_APPROVED);
            }
            bookingServiceNeedToPay.forEach(tourBookingService1 -> tourBookingService1.setStatus(TourBookingServiceStatus.PAID));
            bookingServiceRepository.saveAll(bookingServiceNeedToPay);

            Transaction transaction = Transaction.builder()
                    .booking(tourBooking)
                    .amount(requestDTO.getAmount())
                    .category(requestDTO.getTransactionType())
                    .paidBy(requestDTO.getPaidBy())
                    .receivedBy(requestDTO.getReceivedBy())
                    .paymentMethod(requestDTO.getPaymentMethod())
                    .notes(requestDTO.getNotes())
                    .transactionStatus(TransactionStatus.PENDING)
                    .build();

            Transaction transaction1 = transactionRepository.save(transaction);

            Service service = serviceRepository.findById(requestDTO.getServiceId()).orElseThrow(
                    () -> BusinessException.of(SERVICE_NOT_FOUND)
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
            return new GeneralResponse<>(HttpStatus.OK.value(), PAY_SERVICE_SUCCESS, resultDTO);

        } catch (Exception ex) {
            throw BusinessException.of(PAY_SERVICE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getListLocationAndServiceCategory(Long tourScheduleId) {
        try {
            Map<String, Map<Long, String>> resultDTO = new HashMap<>();

            //Get list location
            List<Location> locations = locationRepository.findLocationOfTourByScheduleId(tourScheduleId);
            Map<Long, String> mapLocation = locations.stream()
                    .collect(Collectors.toMap(Location::getId, Location::getName));

            resultDTO.put("locations", mapLocation);

            //Get list service category
            List<ServiceCategory> serviceCategories = serviceCategoryRepository.findByDeletedFalse();
            Map<Long, String> mapServiceCategory = serviceCategories.stream()
                    .collect(Collectors.toMap(ServiceCategory::getId, ServiceCategory::getCategoryName));

            resultDTO.put("serviceCategories", mapServiceCategory);
            return new GeneralResponse<>(HttpStatus.OK.value(), GET_LOCATIONS_CATEGORIES_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_LOCATIONS_CATEGORIES_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<Map<Long, String>> getListServiceProviderByLocationIdAndServiceCategoryId(Long locationId, Long serviceCategoryId) {
        try {
            List<ServiceProvider> providers = providerRepository.findByLocationIdAndServiceCategoryIdAndDeletedFalse(locationId, serviceCategoryId);
            Map<Long, String> resultDTO = providers.stream()
                    .collect(Collectors.toMap(ServiceProvider::getId, ServiceProvider::getName));
            return new GeneralResponse<>(HttpStatus.OK.value(), GET_PROVIDERS_BY_LOCATION_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_PROVIDERS_BY_LOCATION_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<List<ServiceSimpleDTO>> getListServiceByServiceProviderId(Long serviceProviderId, Long serviceCategoryId) {
        try {
            List<Service> services = serviceRepository.findByServiceProviderIdAndServiceCategoryIdAndDeletedFalse(serviceProviderId, serviceCategoryId);
            List<ServiceSimpleDTO> resultDTO = services.stream()
                    .map(serviceMapper::toSimpleDTO)
                    .collect(Collectors.toList());
            return new GeneralResponse<>(HttpStatus.OK.value(), GET_SERVICES_BY_PROVIDER_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_SERVICES_BY_PROVIDER_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getServiceDetail(Long serviceId) {
        try {
            Service service = serviceRepository.findById(serviceId).orElseThrow(
                    () -> BusinessException.of(SERVICE_NOT_FOUND)
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

            return new GeneralResponse<>(HttpStatus.OK.value(), GET_SERVICE_DETAIL_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_SERVICE_DETAIL_FAIL, ex);
        }
    }

    @Transactional
    @Override
    public GeneralResponse<?> addService(AddServiceRequestDTO requestDTO) {
        try {
            Service service = serviceRepository.findById(requestDTO.getServiceId()).orElseThrow(
                    () -> BusinessException.of(SERVICE_NOT_FOUND)
            );
            TourBooking booking = tourBookingRepository.findById(requestDTO.getBookingId()).orElseThrow(
                    () -> BusinessException.of(TOUR_BOOKING_NOT_FOUND)
            );

            checkAuthor(booking.getTourSchedule().getId());
            List<TourBookingService> bookingService = bookingServiceRepository.findByBookingIdAndServiceIdAndTourDayIdAndDeletedFalse(requestDTO.getBookingId(), requestDTO.getServiceId(), requestDTO.getTourDayId());

            TourDay tourDay = tourDayRepository.findById(requestDTO.getTourDayId()).orElseThrow(
                    () -> BusinessException.of(TOUR_DAY_NOT_FOUND)
            );

            //kiểm tra xem dịch vụ đã có trong tour booking chưa
            List<TourBookingService> availableBookingService = bookingService.stream().filter(tourBookingService ->
                    tourBookingService.getStatus().equals(TourBookingServiceStatus.AVAILABLE)
            ).toList();
            if (!availableBookingService.isEmpty()) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, SERVICE_ALREADY_EXISTS, requestDTO);
            } else {
                TourBookingService newBookingService = TourBookingService.builder()
                        .booking(booking)
                        .service(service)
                        .currentQuantity(requestDTO.getAddQuantity())
                        .requestDate(requestDTO.getRequestDate())
                        .deleted(Boolean.FALSE)
                        .reason(requestDTO.getReason())
                        .status(TourBookingServiceStatus.AVAILABLE)
                        .tourDay(tourDay)
                        .build();

                //Kiểm tra loại tour
                TourType tourType = tourRepository.findTourTypeByTourBookingId(requestDTO.getBookingId());

                //Kiểm tra loại tour phải tour Private hay không
                if (tourType.equals(TourType.PRIVATE)) {
                    newBookingService.setStatus(TourBookingServiceStatus.CHECKING);
                }
                bookingServiceRepository.save(newBookingService);

                if (tourType.equals(TourType.SIC)) {
                    //Create transaction for new service
                    int tourPaxId = tourScheduleRepository.findTourPaxIdByScheduleId(booking.getTourSchedule().getId());
                    Optional<TourDayService> tourDayServiceOptional = tourDayServiceRepository.findByTourDayIdAndServiceId(requestDTO.getTourDayId(), requestDTO.getServiceId());
                    double sellingPriceByPax;
                    if (tourDayServiceOptional.isPresent()) {
                        TourDayService tourDayService = tourDayServiceOptional.get();
                        sellingPriceByPax = servicePaxPricingRepository.findSellingPriceByTourDayServiceIdAndTourPaxId(tourDayService.getId(), tourPaxId);
                    } else {
                        sellingPriceByPax = service.getSellingPrice();
                    }

                    Transaction transaction = Transaction.builder()
                            .booking(booking)
                            .amount(sellingPriceByPax * newBookingService.getCurrentQuantity())
                            .category(TransactionType.RECEIPT)
                            .paidBy(booking.getUser().getFullName())
                            .receivedBy("Viet Travel")
                            .paymentMethod(PaymentMethod.BANKING)
                            .notes("Thu phí dịch vụ phát sinh của khách " + booking.getBookingCode()
                                    + " - dịch vụ: " + newBookingService.getService().getName() + ", số lượng: " + newBookingService.getCurrentQuantity())
                            .transactionStatus(TransactionStatus.PENDING)
                            .build();

                    CostAccount.builder()
                            .transaction(transaction)
                            .amount(sellingPriceByPax)
                            .discount(0)
                            .content("Thu phí dịch vụ phát sinh của khách " + booking.getBookingCode()
                                    + " - dịch vụ: " + newBookingService.getService().getName())
                            .quantity(newBookingService.getCurrentQuantity())
                            .finalAmount(sellingPriceByPax * newBookingService.getCurrentQuantity())
                            .status(CostAccountStatus.PENDING)
                            .build();

                    transactionRepository.save(transaction);
                }
            }

            return new GeneralResponse<>(HttpStatus.OK.value(), ADD_SERVICE_SUCCESS, requestDTO);
        } catch (Exception ex) {
            throw BusinessException.of(ADD_SERVICE_FAIL, ex);
        }
    }

    @Transactional
    @Override
    public GeneralResponse<?> previewMail(PreviewMailDTO previewMailDTO) {
        try {
            TourBookingService bookingService = bookingServiceRepository.findById(previewMailDTO.getBookingServiceId()).orElseThrow(
                    () -> BusinessException.of(BOOKING_SERVICE_NOT_FOUND)
            );
            //Kiểm tra trạng thái của service booking
            if (!(bookingService.getStatus().equals(TourBookingServiceStatus.CHECKING)
                    || bookingService.getStatus().equals(TourBookingServiceStatus.REJECTED))
            ) {
                throw BusinessException.of(SERVICE_STATUS_CANNOT_SEND_EMAIL);
            }
            Service service = serviceRepository.findById(previewMailDTO.getServiceId()).orElseThrow(
                    () -> BusinessException.of(SERVICE_NOT_FOUND)
            );

            ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
                    () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
            );

            String emailContent = MessageFormat.format(
                    emailOrderServiceContent,
                    serviceProvider.getName(),
                    service.getName(),
                    previewMailDTO.getOrderQuantity(),
                    previewMailDTO.getRequestDate(),
                    previewMailDTO.getOrderQuantity() * service.getNettPrice()
            );
            String emailSubject = MessageFormat.format(
                    emailOrderServiceSubject,
                    serviceProvider.getId()
            );
            MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
                    .bookingServiceId(previewMailDTO.getBookingServiceId())
                    .providerId(serviceProvider.getId())
                    .providerName(serviceProvider.getName())
                    .providerEmail(serviceProvider.getEmail())
                    .emailSubject(emailSubject)
                    .emailContent(emailContent)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), PREVIEW_MAIL_SUCCESS, mailServiceDTO);
        } catch (Exception ex) {
            throw BusinessException.of(PREVIEW_MAIL_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getListBookingForAddService(Long scheduleId) {
        try {
            checkAuthor(scheduleId);
            List<TourBooking> bookings = tourBookingRepository.findByTourSchedule_Id(scheduleId);
            List<TourBookingSimpleDTO> resultDTO = bookings.stream()
                    .map(booking ->
                            TourBookingSimpleDTO.builder()
                                    .bookingId(booking.getId())
                                    .bookingCode(booking.getBookingCode())
                                    .customerName(booking.getUser().getFullName())
                                    .build()
                    ).collect(Collectors.toList());
            return new GeneralResponse<>(HttpStatus.OK.value(), GET_BOOKING_LIST_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_BOOKING_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> cancelService(Long tourBookingServiceId) {
        try {
            checkAuthorByTourBookingService(tourBookingServiceId);
            TourBookingService bookingService = bookingServiceRepository.findById(tourBookingServiceId).orElseThrow(
                    () -> BusinessException.of(BOOKING_SERVICE_NOT_FOUND)
            );
            TourBookingServiceStatus currentStatus = bookingService.getStatus();

            if (currentStatus.equals(TourBookingServiceStatus.PENDING)
                    || currentStatus.equals(TourBookingServiceStatus.APPROVED)) {

                //Gửi mail hủy cho nhà cung cấp (chỉ là thông báo)
                Service service = serviceRepository.findById(bookingService.getService().getId()).orElseThrow(
                        () -> BusinessException.of(SERVICE_NOT_FOUND)
                );

                ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
                        () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
                );
                String content = MessageFormat.format(emailCancelServiceContent,
                        serviceProvider.getName(),
                        service.getName(),
                        bookingService.getCurrentQuantity(),
                        bookingService.getRequestDate(),
                        bookingService.getCurrentQuantity() * service.getNettPrice());

                String subject = MessageFormat.format(emailCancelServiceSubject, serviceProvider.getId());

                MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
                        .bookingServiceId(tourBookingServiceId)
                        .providerId(serviceProvider.getId())
                        .providerName(serviceProvider.getName())
                        .providerEmail(serviceProvider.getEmail())
                        .emailSubject(subject)
                        .emailContent(content)
                        .build();
                emailService.sendMailServiceProvider(mailServiceDTO);
            }


            bookingService.setStatus(TourBookingServiceStatus.CANCELLED);

            bookingServiceRepository.save(bookingService);

//            TourBookingServiceCommonDTO resultDTO = bookingServiceMapper.toCommonDTO(bookingService);

            return new GeneralResponse<>(HttpStatus.OK.value(), CANCEL_SERVICE_SUCCESS, tourBookingServiceId);
        } catch (Exception ex) {
            throw BusinessException.of(CANCEL_SERVICE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> updateServiceQuantity(ServiceQuantityUpdateDTO requestDTO) {
        try {
            checkAuthorByTourBookingService(requestDTO.getTourBookingServiceId());

            TourBookingService bookingService = bookingServiceRepository.findById(requestDTO.getTourBookingServiceId()).orElseThrow(
                    () -> BusinessException.of(BOOKING_SERVICE_NOT_FOUND)
            );
            //Kiểm tra trạng thái (đã thanh toán không thể update)
            if (bookingService.getStatus().equals(TourBookingServiceStatus.PAID)) {
                throw BusinessException.of("Không thể cập nhật số lượng");
            }

            if (requestDTO.getNewQuantity() <= 0 ||
                    requestDTO.getNewQuantity() == bookingService.getCurrentQuantity()) {
                throw BusinessException.of(INVALID_SERVICE_QUANTITY);
            }

            //check tour type
            TourType tourType = tourRepository.findTourTypeByTourBookingServiceId(requestDTO.getTourBookingServiceId());
            if (!tourType.equals(TourType.SIC)) {

//                //Trường hợp thay đổi số lượng ở trạng thái NOT_ORDERED
//                if (bookingService.getStatus().equals(TourBookingServiceStatus.NOT_ORDERED)) {
//                    throw BusinessException.of("Không được cập nhật dịch vụ");
//                }

                //Trường hợp thay đổi số lượng ở trạng thái PENDING
                if (requestDTO.getNewQuantity() > 0 && bookingService.getStatus().equals(TourBookingServiceStatus.PENDING)) {

                    //Gửi mail thông báo thay đổi cho nhà cung cấp (chỉ là thông báo)
                    Service service = serviceRepository.findById(bookingService.getService().getId()).orElseThrow(
                            () -> BusinessException.of(SERVICE_NOT_FOUND)
                    );

                    ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
                            () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
                    );
                    String content = MessageFormat.format(emailChangeServiceContent,
                            serviceProvider.getName(),
                            service.getName(),
                            bookingService.getCurrentQuantity(),
                            requestDTO.getNewQuantity(),
                            bookingService.getRequestDate(),
                            requestDTO.getNewQuantity() * service.getNettPrice());

                    String subject = MessageFormat.format(emailChangeServiceSubject, serviceProvider.getId());

                    MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
                            .bookingServiceId(requestDTO.getTourBookingServiceId())
                            .providerId(serviceProvider.getId())
                            .providerName(serviceProvider.getName())
                            .providerEmail(serviceProvider.getEmail())
                            .emailSubject(subject)
                            .emailContent(content)
                            .build();
                    emailService.sendMailServiceProvider(mailServiceDTO);

                    bookingService.setCurrentQuantity(requestDTO.getNewQuantity());
                }

                //Trường hợp thay đổi số lượng ở trạng thái AVAILABLE (new)
                else if (requestDTO.getNewQuantity() > 0 && bookingService.getStatus().equals(TourBookingServiceStatus.AVAILABLE)) {
                    bookingService.setStatus(TourBookingServiceStatus.PENDING);
                    bookingService.setRequestedQuantity(requestDTO.getNewQuantity());

                    //Gửi mail thông báo thay đổi cho nhà cung cấp (yêu cầu nhà cung cấp xác nhận)
                    Service service = serviceRepository.findById(bookingService.getService().getId()).orElseThrow(
                            () -> BusinessException.of(SERVICE_NOT_FOUND)
                    );

                    ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
                            () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
                    );
                    String content = MessageFormat.format(emailUpdateServiceContent,
                            serviceProvider.getName(),
                            service.getName(),
                            bookingService.getCurrentQuantity(),
                            requestDTO.getNewQuantity(),
                            bookingService.getRequestDate(),
                            requestDTO.getNewQuantity() * service.getNettPrice());

                    String subject = MessageFormat.format(emailUpdateServiceSubject, serviceProvider.getId());

                    MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
                            .bookingServiceId(requestDTO.getTourBookingServiceId())
                            .providerId(serviceProvider.getId())
                            .providerName(serviceProvider.getName())
                            .providerEmail(serviceProvider.getEmail())
                            .emailSubject(subject)
                            .emailContent(content)
                            .build();
                    emailService.sendMailServiceProvider(mailServiceDTO);

                } else if (requestDTO.getNewQuantity() > 0 && bookingService.getStatus().equals(TourBookingServiceStatus.CHECKING)) {
                    bookingService.setCurrentQuantity(requestDTO.getNewQuantity());
                } else {
                    throw BusinessException.of("Không được cập nhật dịch vụ");
                }
            } else {
                //đổi số lượng đối với tour SIC
                if (bookingService.getStatus().equals(TourBookingServiceStatus.AVAILABLE)) {
                    bookingService.setCurrentQuantity(requestDTO.getNewQuantity());
                } else {
                    throw BusinessException.of("Không được cập nhật dịch vụ");
                }


//                //Gửi mail thông báo thay đổi cho nhà cung cấp (chỉ là thông báo)
//                Service service = serviceRepository.findById(bookingService.getService().getId()).orElseThrow(
//                        () -> BusinessException.of(SERVICE_NOT_FOUND)
//                );
//
//                ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
//                        () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
//                );
//                String content = MessageFormat.format(emailChangeServiceContent,
//                        serviceProvider.getName(),
//                        service.getName(),
//                        bookingService.getCurrentQuantity(),
//                        bookingService.getRequestedQuantity(),
//                        bookingService.getRequestDate(),
//                        bookingService.getRequestedQuantity() * service.getNettPrice());
//
//                String subject = MessageFormat.format(emailChangeServiceSubject, serviceProvider.getId());
//
//                MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
//                        .bookingServiceId(bookingService.getId())
//                        .providerId(serviceProvider.getId())
//                        .providerName(serviceProvider.getName())
//                        .providerEmail(serviceProvider.getEmail())
//                        .emailSubject(subject)
//                        .emailContent(content)
//                        .build();
//                emailService.sendMailServiceProvider(mailServiceDTO);
            }
            bookingServiceRepository.save(bookingService);

            TourBooking booking = bookingService.getBooking();
            Service service = bookingService.getService();
            Tour tour = (booking != null) ? booking.getTour() : null;
            TourSchedule tourSchedule = (booking != null) ? booking.getTourSchedule() : null;
            TourDay tourDay = bookingService.getTourDay();

            ChangeServiceDetailDTO resultDTO = ChangeServiceDetailDTO.builder()
                    .tourBookingServiceId(requestDTO.getTourBookingServiceId())
                    .tourName((tour != null) ? tour.getName() : null)
                    .tourType((tour != null) ? tour.getTourType().toString() : null)
                    .startDate((tourSchedule != null) ? tourSchedule.getStartDate() : null)
                    .endDate((tourSchedule != null) ? tourSchedule.getEndDate() : null)
                    .dayNumber((tourDay != null) ? tourDay.getDayNumber() : null)
                    .bookingCode((booking != null) ? booking.getBookingCode() : null)
                    .status((bookingService.getStatus() != null) ? bookingService.getStatus().name() : null)
                    .reason(bookingService.getReason())
                    .proposer((booking != null && booking.getUser() != null) ? booking.getUser().getFullName() : null)
                    .updatedAt(bookingService.getUpdatedAt())
                    .serviceName((service != null) ? service.getName() : null)
                    .nettPrice((service != null) ? service.getNettPrice() : null)
                    .requestQuantity(bookingService.getRequestedQuantity())
                    .currentQuantity(bookingService.getCurrentQuantity())
                    .totalPrice(Optional.ofNullable(service)
                            .map(s -> s.getNettPrice() * bookingService.getCurrentQuantity())
                            .orElse(null))
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), UPDATE_SERVICE_QUANTITY_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_SERVICE_QUANTITY_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> sendAccountant(Long tourScheduleId) {
        try {
            checkAuthor(tourScheduleId);
            TourSchedule tourSchedule = tourScheduleRepository.findById(tourScheduleId).orElseThrow(
                    () -> BusinessException.of(NO_TOUR_SCHEDULE_FOUND)
            );

            //Check status of tour schedule
            if (!tourSchedule.getStatus().equals(TourScheduleStatus.ONGOING)) {
                throw BusinessException.of(TOUR_SCHEDULE_NOT_ONGOING);
            }

            List<TourBookingService> tourBookingServiceStatusList = bookingServiceRepository.findByScheduleId(tourSchedule.getId());
            for (TourBookingService item : tourBookingServiceStatusList) {
                if (item.getStatus().equals(TourBookingServiceStatus.AVAILABLE)
                ||item.getStatus().equals(TourBookingServiceStatus.PENDING)
                ||item.getStatus().equals(TourBookingServiceStatus.APPROVED)
                ||item.getStatus().equals(TourBookingServiceStatus.CHECKING)
                ||item.getStatus().equals(TourBookingServiceStatus.NOT_ORDERED)
                ||item.getStatus().equals(TourBookingServiceStatus.CANCEL_REQUEST)) {
                    throw BusinessException.of("Không thể chuyển quyết toán");
                }
            }
            tourSchedule.setStatus(TourScheduleStatus.SETTLEMENT);
            tourScheduleRepository.save(tourSchedule);
            return GeneralResponse.of(tourScheduleId);
        } catch (Exception ex) {
            throw BusinessException.of(SEND_ACCOUNTANT_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<OperatorTourDTO>>> getListTourPrivate(int page, int size, String keyword, String status, String orderDate) {
        try {
            Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Specification<TourSchedule> spec = buildSearchSpecification(keyword, status, TourType.PRIVATE);
//                    .and((root, query, criteriaBuilder) -> {
//                        Join<TourSchedule, Tour> tourJoin = root.join("tour");
//                        return criteriaBuilder.equal(tourJoin.get("tourType"), TourType.PRIVATE);
//                    });

            Page<TourSchedule> tourPage = tourScheduleRepository.findAll(spec, pageable);

            // Map to DTO
            List<OperatorTourDTO> operatorTourDTOS = tourPage.getContent().stream()
                    .map(tourSchedule ->
                            OperatorTourDTO.builder()
                                    .scheduleId(tourSchedule.getId())
                                    .startDate(tourSchedule.getStartDate())
                                    .endDate(tourSchedule.getEndDate())
                                    .status(tourSchedule.getStatus().toString())
                                    .tourName(tourSchedule.getTour().getName())
                                    .tourGuide(Optional.ofNullable(tourSchedule.getTourGuide()).map(User::getFullName).orElse(null))
                                    .operator(Optional.ofNullable(tourSchedule.getOperator()).map(User::getFullName).orElse(null))
                                    .maxPax(tourSchedule.getTourPax().getMaxPax())
                                    .build()
                    )
                    .collect(Collectors.toList());

            return buildPagedResponse(tourPage, operatorTourDTOS);
        } catch (Exception ex) {
            throw BusinessException.of(OPERATOR_GET_PRIVATE_TOUR_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getListTourDayOfSchedule(Long scheduleId) {
        try {
            tourScheduleRepository.findById(scheduleId).orElseThrow(
                    () -> BusinessException.of(TOUR_SCHEDULE_NOT_FOUND)
            );

            List<TourDay> tourDays = tourDayRepository.findListTourDayByScheduleId(scheduleId);

            List<PublicTourDayDTO> resultDTO = tourDays.stream().map(tourDayMapper::toPublicTourDayDTO).collect(Collectors.toList());
            return new GeneralResponse<>(HttpStatus.OK.value(), GET_TOUR_DAY_LIST_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_DAY_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> sendMailToProvider(MailServiceDTO mailServiceDTO) {
        try {
            TourBookingService bookingService = bookingServiceRepository.findById(mailServiceDTO.getBookingServiceId()).orElseThrow(
                    () -> BusinessException.of(BOOKING_SERVICE_NOT_FOUND)
            );

            //Kiểm tra trạng thái của service booking
            if (!bookingService.getStatus().equals(TourBookingServiceStatus.CHECKING)) {
                throw BusinessException.of(SERVICE_STATUS_CANNOT_SEND_EMAIL);
            }

            emailService.sendMailServiceProvider(mailServiceDTO);
            bookingService.setStatus(TourBookingServiceStatus.PENDING);
            bookingServiceRepository.save(bookingService);
            return new GeneralResponse<>(HttpStatus.OK.value(), SEND_MAIL_TO_PROVIDER_SUCCESS, mailServiceDTO);
        } catch (Exception ex) {
            throw BusinessException.of(SEND_MAIL_TO_PROVIDER_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getListServiceRequest(int page, int size) {
        try {
            Long currentOperatorId = getCurrentUserOperatorId();
            Pageable pageable = PageRequest.of(page, size);
            List<TourBookingServiceStatus> bookingServiceStatuses = new ArrayList<>();
            bookingServiceStatuses.add(TourBookingServiceStatus.CHECKING);
            bookingServiceStatuses.add(TourBookingServiceStatus.CANCEL_REQUEST);
            Page<TourBookingService> bookingServicePage = bookingServiceRepository.findByRequestedQuantityGreaterThanOrStatus(currentOperatorId, bookingServiceStatuses, pageable);
            List<ChangeServiceDTO> resultDTO = bookingServicePage.getContent().stream()
                    .map(bookingServiceMapper::toChangeServiceDTO
                    )
                    .collect(Collectors.toList());

            return buildPagedResponseServiceRequest(bookingServicePage, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_SERVICE_REQUEST_LIST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getChangeServiceRequestDetail(Long tourBookingServiceId) {
        try {
            TourBookingService bookingService = bookingServiceRepository.findByIdWithDetails(tourBookingServiceId)
                    .orElseThrow(() -> BusinessException.of(BOOKING_SERVICE_NOT_FOUND));

            TourBooking booking = bookingService.getBooking();
            Service service = bookingService.getService();
            Tour tour = (booking != null) ? booking.getTour() : null;
            TourSchedule tourSchedule = (booking != null) ? booking.getTourSchedule() : null;
            TourDay tourDay = bookingService.getTourDay();

            ChangeServiceDetailDTO resultDTO = ChangeServiceDetailDTO.builder()
                    .tourBookingServiceId(tourBookingServiceId)
                    .tourName((tour != null) ? tour.getName() : null)
                    .tourType((tour != null) ? tour.getTourType().toString() : null)
                    .startDate((tourSchedule != null) ? tourSchedule.getStartDate() : null)
                    .endDate((tourSchedule != null) ? tourSchedule.getEndDate() : null)
                    .dayNumber((tourDay != null) ? tourDay.getDayNumber() : null)
                    .bookingCode((booking != null) ? booking.getBookingCode() : null)
                    .status((bookingService.getStatus() != null) ? bookingService.getStatus().name() : null)
                    .reason(bookingService.getReason())
                    .proposer((booking != null && booking.getUser() != null) ? booking.getUser().getFullName() : null)
                    .updatedAt(bookingService.getUpdatedAt())
                    .serviceName((service != null) ? service.getName() : null)
                    .nettPrice((service != null) ? service.getNettPrice() : null)
                    .requestQuantity(bookingService.getRequestedQuantity())
                    .currentQuantity(bookingService.getCurrentQuantity())
                    .totalPrice(Optional.ofNullable(service)
                            .map(s -> s.getNettPrice() * bookingService.getCurrentQuantity())
                            .orElse(null))
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), GET_SERVICE_REQUEST_DETAIL_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_SERVICE_REQUEST_DETAIL_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> rejectServiceRequest(Long tourBookingServiceId) {
        try {
            TourBookingService bookingService = bookingServiceRepository.findById(tourBookingServiceId).orElseThrow(
                    () -> BusinessException.of(BOOKING_SERVICE_NOT_FOUND)
            );

            //Trường hợp yêu cầu hủy dịch vụ
            if (bookingService.getStatus().equals(TourBookingServiceStatus.CANCEL_REQUEST)) {
                bookingService.setStatus(TourBookingServiceStatus.REJECTED_BY_OPERATOR);
            }

            //Trường hợp kiểm tra khả dụng của dịch vụ
            if (bookingService.getStatus().equals(TourBookingServiceStatus.CHECKING)) {
                bookingService.setStatus(TourBookingServiceStatus.NOT_AVAILABLE);
            }

            //Trường hợp thay đổi số lượng
            if (bookingService.getRequestedQuantity() > 0) {
//                bookingService.setRequestedQuantity(0);
                bookingService.setStatus(TourBookingServiceStatus.REJECTED_BY_OPERATOR);

                //Send email to sale
                User sale = bookingService.getBooking().getSale();
                Service service = bookingService.getService();

                String content = MessageFormat.format(emailRejectServiceUpdateContent,
                        sale.getFullName(),
                        service.getName(),
                        bookingService.getCurrentQuantity(),
                        bookingService.getRequestedQuantity(),
                        bookingService.getRequestDate());

                MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
                        .bookingServiceId(tourBookingServiceId)
                        .providerId(sale.getId())
                        .providerName(sale.getFullName())
                        .providerEmail(sale.getEmail())
                        .emailSubject(emailRejectServiceUpdateSubject)
                        .emailContent(content)
                        .build();
                emailService.sendMailServiceProvider(mailServiceDTO);

            }

            bookingServiceRepository.save(bookingService);

            TourBooking booking = bookingService.getBooking();
            Service service = bookingService.getService();
            Tour tour = (booking != null) ? booking.getTour() : null;
            TourSchedule tourSchedule = (booking != null) ? booking.getTourSchedule() : null;
            TourDay tourDay = bookingService.getTourDay();

            ChangeServiceDetailDTO resultDTO = ChangeServiceDetailDTO.builder()
                    .tourBookingServiceId(tourBookingServiceId)
                    .tourName((tour != null) ? tour.getName() : null)
                    .tourType((tour != null) ? tour.getTourType().toString() : null)
                    .startDate((tourSchedule != null) ? tourSchedule.getStartDate() : null)
                    .endDate((tourSchedule != null) ? tourSchedule.getEndDate() : null)
                    .dayNumber((tourDay != null) ? tourDay.getDayNumber() : null)
                    .bookingCode((booking != null) ? booking.getBookingCode() : null)
                    .status((bookingService.getStatus() != null) ? bookingService.getStatus().name() : null)
                    .reason(bookingService.getReason())
                    .proposer((booking != null && booking.getUser() != null) ? booking.getUser().getFullName() : null)
                    .updatedAt(bookingService.getUpdatedAt())
                    .serviceName((service != null) ? service.getName() : null)
                    .nettPrice((service != null) ? service.getNettPrice() : null)
                    .requestQuantity(bookingService.getRequestedQuantity())
                    .currentQuantity(bookingService.getCurrentQuantity())
                    .totalPrice(Optional.ofNullable(service)
                            .map(s -> s.getNettPrice() * bookingService.getCurrentQuantity())
                            .orElse(null))
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), REJECT_SERVICE_REQUEST_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(REJECT_SERVICE_REQUEST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> approveServiceRequest(Long tourBookingServiceId) {
        try {
            TourBookingService bookingService = bookingServiceRepository.findById(tourBookingServiceId).orElseThrow(
                    () -> BusinessException.of(BOOKING_SERVICE_NOT_FOUND)
            );

            TourType tourType = tourRepository.findTourTypeByTourBookingServiceId(tourBookingServiceId);

            //Trường hợp yêu cầu hủy dịch vụ
            if (bookingService.getStatus().equals(TourBookingServiceStatus.CANCEL_REQUEST)) {
                bookingService.setStatus(TourBookingServiceStatus.CANCELLED);
            }

            //Kiểm tra loại tour phải tour SIC hay không
            if (tourType.equals(TourType.SIC)) {
                bookingService.setCurrentQuantity(bookingService.getRequestedQuantity());
                bookingService.setRequestedQuantity(0);
                bookingService.setStatus(TourBookingServiceStatus.AVAILABLE);

                //Gửi mail thông báo thay đổi cho nhà cung cấp (chỉ là thông báo)
                Service service = serviceRepository.findById(bookingService.getService().getId()).orElseThrow(
                        () -> BusinessException.of(SERVICE_NOT_FOUND)
                );

                ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
                        () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
                );
                String content = MessageFormat.format(emailChangeServiceContent,
                        serviceProvider.getName(),
                        service.getName(),
                        bookingService.getCurrentQuantity(),
                        bookingService.getRequestedQuantity(),
                        bookingService.getRequestDate(),
                        bookingService.getRequestedQuantity() * service.getNettPrice());

                String subject = MessageFormat.format(emailChangeServiceSubject, serviceProvider.getId());

                MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
                        .bookingServiceId(tourBookingServiceId)
                        .providerId(serviceProvider.getId())
                        .providerName(serviceProvider.getName())
                        .providerEmail(serviceProvider.getEmail())
                        .emailSubject(subject)
                        .emailContent(content)
                        .build();
                emailService.sendMailServiceProvider(mailServiceDTO);
            }

            if (!tourType.equals(TourType.SIC)) {

                //Trường hợp kiểm tra khả dụng của dịch vụ
                if (bookingService.getStatus().equals(TourBookingServiceStatus.CHECKING)) {

//                //Gửi mail đặt hàng với nhà cung cấp (yêu cầu nhà cung cấp xác nhận)
//                Service service = serviceRepository.findById(bookingService.getService().getId()).orElseThrow(
//                        () -> BusinessException.of(SERVICE_NOT_FOUND)
//                );
//
//                ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
//                        () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
//                );
//                String content = MessageFormat.format(emailOrderServiceContent,
//                        serviceProvider.getName(),
//                        service.getName(),
//                        bookingService.getCurrentQuantity(),
//                        bookingService.getRequestDate(),
//                        bookingService.getCurrentQuantity() * service.getNettPrice());
//
//                String subject = MessageFormat.format(emailOrderServiceSubject, serviceProvider.getId());
//
//                MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
//                        .bookingServiceId(tourBookingServiceId)
//                        .providerId(serviceProvider.getId())
//                        .providerName(serviceProvider.getName())
//                        .providerEmail(serviceProvider.getEmail())
//                        .emailSubject(subject)
//                        .emailContent(content)
//                        .build();
//                emailService.sendMailServiceProvider(mailServiceDTO);
                    bookingService.setStatus(TourBookingServiceStatus.PENDING);
                }

//            //Trường hợp thay đổi số lượng ở trạng thái AVAILABLE
//            if (bookingService.getRequestedQuantity() > 0 && bookingService.getStatus().equals(TourBookingServiceStatus.AVAILABLE)) {
//                bookingService.setCurrentQuantity(bookingService.getRequestedQuantity());
//                bookingService.setRequestedQuantity(0);
//            }

                //Trường hợp thay đổi số lượng ở trạng thái PENDING
                if (bookingService.getRequestedQuantity() > 0 && bookingService.getStatus().equals(TourBookingServiceStatus.PENDING)) {
                    bookingService.setCurrentQuantity(bookingService.getRequestedQuantity());
                    bookingService.setRequestedQuantity(0);

                    //Gửi mail thông báo thay đổi cho nhà cung cấp (chỉ là thông báo)
                    Service service = serviceRepository.findById(bookingService.getService().getId()).orElseThrow(
                            () -> BusinessException.of(SERVICE_NOT_FOUND)
                    );

                    ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
                            () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
                    );
                    String content = MessageFormat.format(emailChangeServiceContent,
                            serviceProvider.getName(),
                            service.getName(),
                            bookingService.getCurrentQuantity(),
                            bookingService.getRequestedQuantity(),
                            bookingService.getRequestDate(),
                            bookingService.getRequestedQuantity() * service.getNettPrice());

                    String subject = MessageFormat.format(emailChangeServiceSubject, serviceProvider.getId());

                    MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
                            .bookingServiceId(tourBookingServiceId)
                            .providerId(serviceProvider.getId())
                            .providerName(serviceProvider.getName())
                            .providerEmail(serviceProvider.getEmail())
                            .emailSubject(subject)
                            .emailContent(content)
                            .build();
                    emailService.sendMailServiceProvider(mailServiceDTO);
                }

                //Trường hợp thay đổi số lượng ở trạng thái AVAILABLE
                if (bookingService.getRequestedQuantity() > 0 && bookingService.getStatus().equals(TourBookingServiceStatus.AVAILABLE)) {
                    bookingService.setStatus(TourBookingServiceStatus.PENDING);

                    //Gửi mail thông báo thay đổi cho nhà cung cấp (yêu cầu nhà cung cấp xác nhận)
                    Service service = serviceRepository.findById(bookingService.getService().getId()).orElseThrow(
                            () -> BusinessException.of(SERVICE_NOT_FOUND)
                    );

                    ServiceProvider serviceProvider = providerRepository.findById(service.getServiceProvider().getId()).orElseThrow(
                            () -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND)
                    );
                    String content = MessageFormat.format(emailUpdateServiceContent,
                            serviceProvider.getName(),
                            service.getName(),
                            bookingService.getCurrentQuantity(),
                            bookingService.getRequestedQuantity(),
                            bookingService.getRequestDate(),
                            bookingService.getRequestedQuantity() * service.getNettPrice());

                    String subject = MessageFormat.format(emailUpdateServiceSubject, serviceProvider.getId());

                    MailServiceDTO mailServiceDTO = MailServiceDTO.builder()
                            .bookingServiceId(tourBookingServiceId)
                            .providerId(serviceProvider.getId())
                            .providerName(serviceProvider.getName())
                            .providerEmail(serviceProvider.getEmail())
                            .emailSubject(subject)
                            .emailContent(content)
                            .build();
                    emailService.sendMailServiceProvider(mailServiceDTO);
                }
            }
            bookingServiceRepository.save(bookingService);

            TourBooking booking = bookingService.getBooking();
            Service service = bookingService.getService();
            Tour tour = (booking != null) ? booking.getTour() : null;
            TourSchedule tourSchedule = (booking != null) ? booking.getTourSchedule() : null;
            TourDay tourDay = bookingService.getTourDay();

            ChangeServiceDetailDTO resultDTO = ChangeServiceDetailDTO.builder()
                    .tourBookingServiceId(tourBookingServiceId)
                    .tourName((tour != null) ? tour.getName() : null)
                    .tourType((tour != null) ? tour.getTourType().toString() : null)
                    .startDate((tourSchedule != null) ? tourSchedule.getStartDate() : null)
                    .endDate((tourSchedule != null) ? tourSchedule.getEndDate() : null)
                    .dayNumber((tourDay != null) ? tourDay.getDayNumber() : null)
                    .bookingCode((booking != null) ? booking.getBookingCode() : null)
                    .status((bookingService.getStatus() != null) ? bookingService.getStatus().name() : null)
                    .reason(bookingService.getReason())
                    .proposer((booking != null && booking.getUser() != null) ? booking.getUser().getFullName() : null)
                    .updatedAt(bookingService.getUpdatedAt())
                    .serviceName((service != null) ? service.getName() : null)
                    .nettPrice((service != null) ? service.getNettPrice() : null)
                    .requestQuantity(bookingService.getRequestedQuantity())
                    .currentQuantity(bookingService.getCurrentQuantity())
                    .totalPrice(Optional.ofNullable(service)
                            .map(s -> s.getNettPrice() * bookingService.getCurrentQuantity())
                            .orElse(null))
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), APPROVE_SERVICE_REQUEST_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(APPROVE_SERVICE_REQUEST_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getTourSummary(Long scheduleId) {
        try {
            checkAuthor(scheduleId);
            List<TransactionType> transactionReceiptTypes = new ArrayList<>();
            transactionReceiptTypes.add(TransactionType.RECEIPT);
            transactionReceiptTypes.add(TransactionType.COLLECTION);

            List<TransactionType> transactionPaymentTypes = new ArrayList<>();
            transactionPaymentTypes.add(TransactionType.PAYMENT);
            transactionPaymentTypes.add(TransactionType.ADVANCED);
            //Tìm tất cả các booking thuộc schedule
            List<TourBooking> bookings = tourBookingRepository.findByTourSchedule_Id(scheduleId);

            //Tìm tất cả transaction thuộc schedule
            List<Transaction> transactions = transactionRepository.findAllByBookingIn(bookings);

            //Tìm số tiền công ty đã thu của cả lịch trình
            BigDecimal receiptedAmount = transactionRepository.findAmountByTransactionCategoryAndCostAccountStatusIn(
                    transactions,
                    TransactionType.RECEIPT, CostAccountStatus.PAID);

            //Tìm số tiền HDV đã thu hộ của cả lịch trình
            BigDecimal collectionAmount = transactionRepository.findAmountByTransactionCategoryAndCostAccountStatusIn(
                    transactions,
                    TransactionType.COLLECTION, CostAccountStatus.PAID);

            //Tìm tổng số tiền phải thu
            BigDecimal totalReceiptAmount = transactionRepository.findTotalAmountByTransactionCategoryIn(
                    transactions,
                    transactionReceiptTypes
            );

            //Tìm số tiền công ty đã chi
            BigDecimal paymentAmount = transactionRepository.findAmountByTransactionCategoryAndCostAccountStatusIn(
                    transactions,
                    TransactionType.PAYMENT, CostAccountStatus.PAID);

            //Tìm số tiền HDV đã chi
            BigDecimal advanceAmount = transactionRepository.findAmountByTransactionCategoryAndCostAccountStatusIn(
                    transactions,
                    TransactionType.ADVANCED, CostAccountStatus.PAID);

            //Tìm tổng số tiền phải chi
            BigDecimal totalPaymentAmount = transactionRepository.findTotalAmountByTransactionCategoryIn(
                    transactions,
                    transactionPaymentTypes
            );

            //Tìm số tiền ước tính phải chi cho cả tour

            //tìm danh sahcs ngày của tour
            List<TourDay> tourDayList = tourDayRepository.findListTourDayByScheduleId(scheduleId);
            List<Long> tourDayIds = tourDayList.stream().map(TourDay::getId).toList();
            List<TourDayService> tourDayServices = tourDayServiceRepository.findByTourDayIdInExceptTransportAndHotel(tourDayIds);
            List<Service> tourServices = tourDayServices.stream().map(TourDayService::getService).toList();
            BigDecimal estimatedPaymentAmountUnit = tourServices.stream()
                    .map(ts -> {
                        return BigDecimal.valueOf(ts.getNettPrice());
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal passengerNumber = BigDecimal.valueOf(bookings.stream().map(TourBooking::getSeats).reduce(0, Integer::sum));
            BigDecimal estimatedPaymentAmount = estimatedPaymentAmountUnit.multiply(passengerNumber);

            //tinh tien hotel
            List<TourDayService> tourDayServicesHotel = tourDayServiceRepository.findByTourDayIdInHotel(tourDayIds);
            BigDecimal estimatedPaymentAmountHotelUnit = tourDayServicesHotel.stream().map(result -> {
                return BigDecimal.valueOf(result.getService().getNettPrice());
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal estimatedPaymentAmountHotel = estimatedPaymentAmountHotelUnit.multiply(passengerNumber.divide(BigDecimal.valueOf(2), RoundingMode.CEILING));
            estimatedPaymentAmount = estimatedPaymentAmount.add(estimatedPaymentAmountHotel);


//            List<Long> tourServiceIds = tourServices.stream().map(Service::getId).toList();
//            List<TourBookingService> tourBookingServices = bookingServiceRepository.findByScheduleId(scheduleId);
//            BigDecimal estimatedPaymentAmount = tourBookingServices.stream()
//                    .filter(tbs -> tourServiceIds.contains(tbs.getService().getId()))
//                    .map(tbs -> {
//                        Double nettPrice = tbs.getService().getNettPrice();
//                        return BigDecimal.valueOf(nettPrice).multiply(BigDecimal.valueOf(tbs.getCurrentQuantity()));
//                    })
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);

//            int paxId = tourScheduleRepository.findTourPaxIdByScheduleId(scheduleId);
//            TourSchedule tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow();
//            int minPax = tourSchedule.getTourPax().getMinPax();
//            List<ServicePaxPricing> servicePaxPricings = servicePaxPricingRepository.findByTourDayServiceIdInAndTourPaxId(tourDayServiceIds, paxId);
//            BigDecimal estimatedPaymentAmount = tourDayServices.stream().map(result ->{
//                return BigDecimal.valueOf(result.getService().getNettPrice());
//            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            //tim tien xe
            List<TourDayService> tourDayServicesTransport = tourDayServiceRepository.findByTourDayIdInTransport(tourDayIds);
//            List<Long> tourDayServiceTransportIds = tourDayServicesTransport.stream().map(TourDayService::getId).toList();
//            List<ServicePaxPricing> serviceTransportPaxPricings = servicePaxPricingRepository.findByTourDayServiceIdInAndTourPaxId(tourDayServiceTransportIds, paxId);
            BigDecimal estimatedPaymentAmountTransport = tourDayServicesTransport.stream().map(result -> {
                return BigDecimal.valueOf(result.getService().getNettPrice());
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            estimatedPaymentAmount = estimatedPaymentAmount.add(estimatedPaymentAmountTransport);


//            List<TourBookingServiceStatus> tourBookingServiceStatusList = new ArrayList<>();
//            tourBookingServiceStatusList.add(TourBookingServiceStatus.NOT_AVAILABLE);
//            tourBookingServiceStatusList.add(TourBookingServiceStatus.REJECTED);
//            tourBookingServiceStatusList.add(TourBookingServiceStatus.REJECTED_BY_OPERATOR);
//            tourBookingServiceStatusList.add(TourBookingServiceStatus.CANCELLED);
//            List<Object[]> services = serviceRepository.findAllServicesWithQuantityInTourSchedule(scheduleId, tourBookingServiceStatusList);
//            BigDecimal estimatedPaymentAmount = services.stream()
//                    .map(result -> {
//                        Service service = (Service) result[0];
//                        Integer quantity = (Integer) result[1];
//                        return BigDecimal.valueOf(service.getNettPrice() * quantity);
//                    })
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
////            //Tìm tiền xe => thêm vào ước tính chi để ra cuối cùng
//            List<BigDecimal> transportFee = serviceRepository.findTransportFeeByScheduleId(scheduleId);
//            for(BigDecimal bigDecimal :transportFee){
//                estimatedPaymentAmount = estimatedPaymentAmount.add(bigDecimal);
//            }


            //Tìm số tiền ước tính thu được cả tour
//            BigDecimal estimateReceiptAmount = transactionRepository.findEstimateReceiptAmount(transactions, transactionReceiptTypes);
//            BigDecimal estimateReceiptAmount = bookings.stream()
//                    .map(result -> {
//                        return BigDecimal.valueOf(result.getTotalAmount());
//                    })
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int paxId = tourScheduleRepository.findTourPaxIdByScheduleId(scheduleId);
            BigDecimal tourSellingPricePerPerson = tourPaxRepository.findSellingPriceByTourPaxIdAndScheduleId(scheduleId, paxId);
            BigDecimal estimateReceiptAmount = tourSellingPricePerPerson.multiply(passengerNumber);

            //Tìm lợi nhuận ước tính
            BigDecimal estimateProfitAmount = estimateReceiptAmount.subtract(estimatedPaymentAmount);

            //Tìm lợi nhuận thực tế
            BigDecimal actualReceiptAmount = receiptedAmount.add(collectionAmount); //Số tiền đã thu thực tế (công ty thu + HDV thu hộ)
            BigDecimal actualPaymentAmount = paymentAmount.add(advanceAmount); //Số tiền đã chi thực tế (công ty chi + HDV đã chi)
            BigDecimal actualProfitAmount = actualReceiptAmount.subtract(actualPaymentAmount);

            TourSummaryDTO resultDTO = TourSummaryDTO.builder()
                    .tourScheduleId(scheduleId)
                    .receiptedAmount(receiptedAmount)
                    .remainingReceiptAmount(totalReceiptAmount.subtract(receiptedAmount).subtract(collectionAmount))
                    .collectionAmount(collectionAmount)
                    .totalReceiptAmount(totalReceiptAmount)
                    .paymentAmount(paymentAmount)
                    .remainingPaymentAmount(totalPaymentAmount.subtract(paymentAmount).subtract(advanceAmount))
                    .advanceAmount(advanceAmount)
                    .totalPaymentAmount(totalPaymentAmount)
                    .estimatedPaymentAmount(estimatedPaymentAmount)
                    .estimateReceiptAmount(estimateReceiptAmount)
                    .estimateProfitAmount(estimateProfitAmount)
                    .actualProfitAmount(actualProfitAmount)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), GET_TOUR_SUMMARY_SUCCESS, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TOUR_SUMMARY_FAIL, ex);
        }
    }


    public Specification<TourSchedule> buildSearchSpecification(String keyword, String status, TourType tourType) {
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
                try {
                    TourScheduleStatus enumStatus = TourScheduleStatus.valueOf(status);
                    predicates.add(cb.equal(root.get("status"), enumStatus));
                } catch (IllegalArgumentException e) {
                    throw BusinessException.of(INVALID_STATUS_VALUE + status, e);
                }
            }
            if (tourType != null) {
                Join<TourSchedule, Tour> tourJoin = root.join("tour", JoinType.LEFT);
                predicates.add(cb.equal(tourJoin.get("tourType"), tourType));
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

        return new GeneralResponse<>(HttpStatus.OK.value(), PAGE_SUCCESS, pagingDTO);
    }

    private <T> GeneralResponse<PagingDTO<List<T>>> buildPagedResponseServiceRequest(Page<TourBookingService> tourPage, List<T> tours) {
        PagingDTO<List<T>> pagingDTO = PagingDTO.<List<T>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tours)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), PAGE_SUCCESS, pagingDTO);
    }

    public Long getCurrentUserOperatorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> BusinessException.of(USER_NOT_FOUND));
            return user.getId();
        }
        throw BusinessException.of(USER_INFO_NOT_FOUND);
    }

    public boolean checkAuthor(Long scheduleId) {
        //Kiểm tra đơn tour có phải của nhà điều hành không
        Long currentOperatorId = getCurrentUserOperatorId();
        TourSchedule tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow(
                () -> BusinessException.of(NO_TOUR_SCHEDULE_FOUND)
        );
        if (tourSchedule.getOperator() != null && (!tourSchedule.getOperator().getId().equals(currentOperatorId))) {
            throw BusinessException.of(UNAUTHORIZED);
        }
        return true;
    }

    private boolean checkAuthorByTourBookingService(Long tourBookingServiceId) {
        //Kiểm tra đơn tour có phải của nhà điều hành không
        Long currentOperatorId = getCurrentUserOperatorId();
        TourSchedule tourSchedule = tourScheduleRepository.findByTourBookingServiceId(tourBookingServiceId);
        if (!tourSchedule.getOperator().getId().equals(currentOperatorId)) {
            throw BusinessException.of(UNAUTHORIZED);
        }
        return true;
    }

}
