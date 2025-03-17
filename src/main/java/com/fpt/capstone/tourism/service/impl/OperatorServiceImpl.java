package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.AssignTourGuideRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourOperationLogRequestDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.repository.*;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Service
@RequiredArgsConstructor
public class OperatorServiceImpl implements OperatorService {
    private final TourScheduleRepository tourScheduleRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourBookingRepository tourBookingRepository;
    private final TourBookingCustomerRepository tourBookingCustomerRepository;
    private final TourOperationLogRepository logRepository;
    private final TransactionRepository transactionRepository;
    private final TourScheduleServiceRepository scheduleServiceRepository;
    private final TourBookingCustomerFullMapper customerFullMapper;
    private final TourOperationLogMapper logMapper;
    private final TransactionMapper transactionMapper;
    private final TagMapper tagMapper;
    private final UserFullInformationMapper userMapper;

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
                            tourSchedule.getStatus(),
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
                    .status(tourSchedule.getStatus())
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
        } catch  (Exception ex) {
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
        } catch  (Exception ex) {
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
        } catch  (Exception ex) {
            throw BusinessException.of("Get list log of tour detail fail", ex);
        }
    }

    @Override
    public GeneralResponse<TourOperationLogDTO> createOperationLog(Long scheduleId, TourOperationLogRequestDTO logRequestDTO) {
        try{
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
        }catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of("Create log fail", ex);
        }
    }

    @Override
    public GeneralResponse<TourOperationLogDTO> deleteOperationLog(Long logId) {
        try{
            TourOperationLog log = logRepository.findById(logId).orElseThrow(() ->
                    BusinessException.of("Not found tour log"));

            log.setDeleted(true);
            log.setUpdatedAt(LocalDateTime.now());
            logRepository.save(log);

            TourOperationLogDTO logDTO = logMapper.toDTO(log);
            return new GeneralResponse<>(HttpStatus.OK.value(), "Delete log success", logDTO);
        }catch (BusinessException be){
            throw be;
        } catch (Exception ex){
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
        } catch  (Exception ex) {
            throw BusinessException.of("Assign tour guide fail", ex);
        }
    }

    @Override
    public GeneralResponse<List<UserResponseDTO>> getListAvailableTourGuide(Long scheduleId) {
        try {
            List<UserResponseDTO> responseList = userRepository.findAvailableTourGuideByScheduleId(scheduleId).stream()
                    .map(userMapper::toResponseDTO).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list available tour guide success", responseList);
        } catch  (Exception ex) {
            throw BusinessException.of("Get list available tour guide fail", ex);
        }
    }

    @Override
    public GeneralResponse<List<OperatorTransactionDTO>> getListTransaction(Long scheduleId) {
        try {
            List<Transaction> transactions = transactionRepository.findAllByTourScheduleId(scheduleId);

            List<OperatorTransactionDTO> responseList = transactions.stream().map(transactionMapper::toDTO)
                    .collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list transaction success", responseList);
        } catch  (Exception ex) {
            throw BusinessException.of("Get list transaction fail", ex);
        }
    }

    @Override
    public GeneralResponse<OperatorServiceListDTO> getListService(Long scheduleId) {
        try {
            //Tìm xem với scheduleId này thì có những service gì
            List<TourScheduleService> scheduleServices = scheduleServiceRepository.findByTourSchedule_Id(scheduleId);

//            List<OperatorTourCustomerDTO> responseList = bookings.stream().map(booking -> {
//                List<TourBookingCustomerDTO> customers = tourBookingCustomerRepository
//                        .findByTourBookingId(booking.getId())
//                        .stream()
//                        .map(customerFullMapper::toDto)
//                        .collect(Collectors.toList());
//
//                OperatorTourCustomerDTO responseDTO = OperatorTourCustomerDTO.builder()
//                        .tourBookingId(booking.getId())
//                        .tourBookingCategory(booking.getTourBookingCategory())
//                        .listCustomer(customers)
//                        .build();
//                return responseDTO;

            //Tìm list tour booking ứng với scheduleId và serviceId
            List<TourBooking> bookings = tourBookingRepository.findByTourSchedule_Id(scheduleId);
            List<OperatorServiceDTO> responseList = scheduleServices.stream()
                    .map(scheduleService -> {



                            }
                    ).collect(Collectors.toList());
            OperatorServiceDTO.builder()
                    .serviceId(scheduleService.getService().getId())
                    .serviceName(scheduleService.getService().getName())
                    .serviceCategory(scheduleService.getService().getServiceCategory().getCategoryName())
                    .usingDate(scheduleService.getService().getStartDate())
                    .requestQuantity(scheduleService.getRequestedQuantity())
                    .currentQuantity(scheduleService.getCurrentQuantity())
                    .bookingStatus(scheduleService.getStatus().toString())
                    .paymentStatus(scheduleServiceRepository.findPaymentStatusByTourBookingId(scheduleService.))
                    .build()


            return new GeneralResponse<>(HttpStatus.OK.value(), "Get list service success", responseList);
        } catch  (Exception ex) {
            throw BusinessException.of("Get list service fail", ex);
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
