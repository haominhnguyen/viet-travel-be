package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.TourScheduleRequestDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.TourScheduleHelper;
import com.fpt.capstone.tourism.mapper.ServiceProviderMapper;
import com.fpt.capstone.tourism.mapper.TourMapper;
import com.fpt.capstone.tourism.mapper.TourScheduleMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.model.enums.TourScheduleStatus;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.TourScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Service
@RequiredArgsConstructor
public class TourScheduleServiceImp implements TourScheduleService {
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourPaxRepository tourPaxRepository;
    private final RoleRepository roleRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    private final TourScheduleMapper tourScheduleMapper;
    private final ServiceProviderMapper serviceProviderMapper;

    private final TourMapper tourMapper;
    private final TourScheduleHelper tourScheduleHelper;
    private final TourBookingRepository tourBookingRepository;
    private final TourBookingCustomerRepository tourBookingCustomerRepository;

    public GeneralResponse<List<EndDateOption>> calculatePossibleEndDates(Long tourId, LocalDateTime startDate) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

        List<EndDateOption> endDateOptions = new ArrayList<>();

        // Basic calculation: add number of days to start date
        LocalDateTime basicEndDate = startDate.plusDays(tour.getNumberDays());
        endDateOptions.add(new EndDateOption(
                basicEndDate,
                String.format(DESC_STANDARD_OPTION,
                        tour.getNumberDays(), tour.getNumberNights()),
                true
        ));

        // Alternative 1: if tour spans weekend, offer option to extend to next weekday
        if (isWeekend(basicEndDate)) {
            LocalDateTime nextWeekdayEnd = getNextWeekday(basicEndDate);
            endDateOptions.add(new EndDateOption(
                    nextWeekdayEnd,
                    String.format(DESC_EXTENDED_WEEKEND_OPTION,
                            ChronoUnit.DAYS.between(startDate, nextWeekdayEnd),
                            ChronoUnit.DAYS.between(startDate, nextWeekdayEnd) - 1),
                    false
            ));
        }

        // Alternative 2: Offer a +1 day option for flexibility
        LocalDateTime extendedEndDate = basicEndDate.plusDays(1);
        endDateOptions.add(new EndDateOption(
                extendedEndDate,
                String.format(DESC_EXTENDED_OPTION,
                        tour.getNumberDays() + 1, tour.getNumberNights() + 1),
                false
        ));

        return GeneralResponse.of(endDateOptions);
    }


    public GeneralResponse<List<OperatorAvailabilityDTO>> findAvailableOperators(
            Long tourId, LocalDateTime startDate, LocalDateTime endDate) {

        // Get OPERATOR role
        Role operatorRole = roleRepository.findByRoleName("OPERATOR")
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, OPERATOR_ROLE_NOT_FOUND));

        // Find users with OPERATOR role
        List<User> operators = userRepository.findUsersByRoleAndActive(operatorRole.getId(), true);

        List<OperatorAvailabilityDTO> availableOperators = new ArrayList<>();

        for (User operator : operators) {
            // Count active tours for this operator in the given period
            int activeToursCount = tourScheduleRepository.countActiveToursForOperator(
                    operator.getId(), startDate, endDate);

            // Only include operators with 3 or fewer active tours
            if (activeToursCount <= 3) {
                availableOperators.add(new OperatorAvailabilityDTO(
                        operator.getId(),
                        operator.getFullName(),
                        activeToursCount
                ));
            }
        }

        // Sort by number of active tours (least busy first)
        availableOperators.sort(Comparator.comparingInt(OperatorAvailabilityDTO::getActiveToursCount));

        return GeneralResponse.of(availableOperators);
    }

    @Override
    public GeneralResponse<TourScheduleBasicResponseDTO> setTourSchedule(TourScheduleRequestDTO requestDTO, User user) {
        Tour tour = tourRepository.findById(requestDTO.getTourId())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

        // Check if the tour status is APPROVED or OPENED
        if (tour.getTourStatus() != TourStatus.APPROVED && tour.getTourStatus() != TourStatus.OPENED) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_STATUS_NOT_APPROVED_OR_OPENED);
        }

        // Get the selected operator
        User operator = userRepository.findById(requestDTO.getOperatorId())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, OPERATOR_NOT_FOUND));

        // Validate that the user is an operator
        boolean isOperator = operator.getUserRoles().stream()
                .anyMatch(userRole -> !userRole.getDeleted() &&
                        userRole.getRole().getRoleName().equals(ROLE_OPERATOR));

        if (!isOperator) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, USER_NOT_OPERATOR);
        }

        // Check if operator is already assigned to this specific tour during the requested period
        boolean isOperatorAlreadyAssigned = tourScheduleRepository.existsByTourIdAndOperatorIdAndDateOverlap(
                tour.getId(), operator.getId(), requestDTO.getStartDate(), requestDTO.getEndDate());

        if (isOperatorAlreadyAssigned) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, OPERATOR_ALREADY_ASSIGNED);
        }

        // Check operator availability (total active tours)
        int activeToursCount = tourScheduleRepository.countActiveToursForOperator(
                operator.getId(), requestDTO.getStartDate(), requestDTO.getEndDate());

        if (activeToursCount > MAX_OPERATOR_TOURS) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, OPERATOR_OVERBOOKED);
        }

        // Get specified TourPax or find an available one if not provided
        TourPax tourPax;
        if (requestDTO.getTourPaxId() != null) {
            // Find the specified pax configuration
            tourPax = tourPaxRepository.findById(requestDTO.getTourPaxId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_PAX_NOT_FOUND));

            // Verify that the pax belongs to this tour
            if (!tourPax.getTour().getId().equals(requestDTO.getTourId())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_MISMATCH);
            }

            // Verify that the pax is not deleted
            if (Boolean.TRUE.equals(tourPax.getDeleted())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_DELETED);
            }

            // Verify that the pax configuration is valid for the specified date range
            if (tourPax.getValidFrom() != null && tourPax.getValidTo() != null) {
                LocalDate scheduleStartDate = requestDTO.getStartDate().toLocalDate();
                LocalDate scheduleEndDate = requestDTO.getEndDate().toLocalDate();

                // Safe conversion of Date to LocalDate
                LocalDate paxValidFrom = new java.sql.Date(tourPax.getValidFrom().getTime()).toLocalDate();
                LocalDate paxValidTo = new java.sql.Date(tourPax.getValidTo().getTime()).toLocalDate();

                // Check if schedule dates fall within pax validity period
                if (scheduleStartDate.isBefore(paxValidFrom) || scheduleEndDate.isAfter(paxValidTo)) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_INVALID_DATES);
                }
            }
        } else {
            // Find a valid pax configuration for the specified dates
            LocalDate scheduleStartDate = requestDTO.getStartDate().toLocalDate();
            LocalDate scheduleEndDate = requestDTO.getEndDate().toLocalDate();

            // Get all non-deleted pax configurations for this tour
            List<TourPax> availablePaxConfigurations = tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(tour.getId());

            if (availablePaxConfigurations.isEmpty()) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_NOT_AVAILABLE);
            }

            // Find a valid pax configuration for the date range
            tourPax = availablePaxConfigurations.stream()
                    .filter(pax -> {
                        // If no validity dates are set, consider it always valid
                        if (pax.getValidFrom() == null || pax.getValidTo() == null) {
                            return true;
                        }

                        // Safe conversion of Date to LocalDate
                        LocalDate paxValidFrom = new java.sql.Date(pax.getValidFrom().getTime()).toLocalDate();
                        LocalDate paxValidTo = new java.sql.Date(pax.getValidTo().getTime()).toLocalDate();

                        // Check if schedule dates fall within pax validity period
                        return !scheduleStartDate.isBefore(paxValidFrom) && !scheduleEndDate.isAfter(paxValidTo);
                    })
                    .findFirst()
                    .orElseThrow(() -> BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_NO_VALID));
        }

        // Create new tour schedule
        TourSchedule tourSchedule = new TourSchedule();
        tourSchedule.setTour(tour);
        tourSchedule.setStartDate(requestDTO.getStartDate());
        tourSchedule.setEndDate(requestDTO.getEndDate());
        tourSchedule.setOperator(operator);
        tourSchedule.setTourPax(tourPax);
        tourSchedule.setStatus(TourScheduleStatus.OPEN);
        tourSchedule.setDeleted(false);

        tourSchedule = tourScheduleRepository.save(tourSchedule);

        tour.setTourStatus(TourStatus.OPENED);
        tourRepository.save(tour);
        return GeneralResponse.of(mapToResponseDTO(tourSchedule), SCHEDULE_CREATED_SUCCESS);
    }

    @Override
    public GeneralResponse<TourScheduleBasicResponseDTO> updateTourSchedule(TourScheduleRequestDTO requestDTO, User user) {
        // Validate that scheduleId is provided
        if (requestDTO.getScheduleId() == null) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, SCHEDULE_ID_REQUIRED);
        }

        TourSchedule existingSchedule = tourScheduleRepository.findById(requestDTO.getScheduleId())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_SCHEDULE_NOT_FOUND));

        if (existingSchedule.getStatus() == TourScheduleStatus.ONGOING) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, SCHEDULE_CANNOT_BE_UPDATED);
        }

        // Find the tour (using existing tour if tourId is not provided)
        Tour tour = (requestDTO.getTourId() != null)
                ? tourRepository.findById(requestDTO.getTourId())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND))
                : existingSchedule.getTour();

        // Get the selected operator (using existing operator if operatorId is not provided)
        User operator = (requestDTO.getOperatorId() != null)
                ? userRepository.findById(requestDTO.getOperatorId())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, OPERATOR_NOT_FOUND))
                : existingSchedule.getOperator();

        // Validate that the user is an operator
        boolean isOperator = operator.getUserRoles().stream()
                .anyMatch(userRole -> !userRole.getDeleted() &&
                        userRole.getRole().getRoleName().equals(ROLE_OPERATOR));

        if (!isOperator) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, USER_NOT_OPERATOR);
        }

        // Set start and end dates (using existing dates if not provided)
        LocalDateTime startDate = (requestDTO.getStartDate() != null)
                ? requestDTO.getStartDate()
                : existingSchedule.getStartDate();

        LocalDateTime endDate = (requestDTO.getEndDate() != null)
                ? requestDTO.getEndDate()
                : existingSchedule.getEndDate();

        // Only check operator availability if we're changing the operator or dates
        if (requestDTO.getOperatorId() != null || requestDTO.getStartDate() != null || requestDTO.getEndDate() != null) {
            // Check if operator is already assigned to another tour during the requested period (excluding this schedule)
            boolean isOperatorAlreadyAssigned = tourScheduleRepository.existsByTourIdAndOperatorIdAndDateOverlapExcludingId(
                    tour.getId(), operator.getId(), startDate, endDate, existingSchedule.getId());

            if (isOperatorAlreadyAssigned) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, OPERATOR_ALREADY_ASSIGNED);
            }

            // Check operator availability (total active tours, excluding this one)
            int activeToursCount = tourScheduleRepository.countActiveToursForOperatorExcludingId(
                    operator.getId(), startDate, endDate, existingSchedule.getId());

            if (activeToursCount > MAX_OPERATOR_TOURS) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, OPERATOR_OVERBOOKED);
            }
        }

        // Process TourPax selection
        TourPax tourPax = existingSchedule.getTourPax();

        if (requestDTO.getTourPaxId() != null) {
            // Find the specified pax configuration
            tourPax = tourPaxRepository.findById(requestDTO.getTourPaxId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_PAX_NOT_FOUND));

            // Verify that the pax belongs to this tour
            if (!tourPax.getTour().getId().equals(tour.getId())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_MISMATCH);
            }

            // Verify that the pax is not deleted
            if (Boolean.TRUE.equals(tourPax.getDeleted())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_DELETED);
            }

            // Verify that the pax configuration is valid for the specified date range
            if (tourPax.getValidFrom() != null && tourPax.getValidTo() != null) {
                LocalDate scheduleStartDate = startDate.toLocalDate();
                LocalDate scheduleEndDate = endDate.toLocalDate();

                // Safe conversion of Date to LocalDate
                LocalDate paxValidFrom = new java.sql.Date(tourPax.getValidFrom().getTime()).toLocalDate();
                LocalDate paxValidTo = new java.sql.Date(tourPax.getValidTo().getTime()).toLocalDate();

                // Check if schedule dates fall within pax validity period
                if (scheduleStartDate.isBefore(paxValidFrom) || scheduleEndDate.isAfter(paxValidTo)) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_INVALID_DATES);
                }
            }
        } else if (!existingSchedule.getTour().getId().equals(tour.getId())) {
            // If tour has changed and no specific tourPax provided, find a valid one
            LocalDate scheduleStartDate = startDate.toLocalDate();
            LocalDate scheduleEndDate = endDate.toLocalDate();

            // Get all non-deleted pax configurations for this tour
            List<TourPax> availablePaxConfigurations = tourPaxRepository.findByTourIdAndDeletedFalseOrderByMinPax(tour.getId());

            if (availablePaxConfigurations.isEmpty()) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_NOT_AVAILABLE);
            }

            // Find a valid pax configuration for the date range
            tourPax = availablePaxConfigurations.stream()
                    .filter(pax -> {
                        // If no validity dates are set, consider it always valid
                        if (pax.getValidFrom() == null || pax.getValidTo() == null) {
                            return true;
                        }
                        // Safe conversion of Date to LocalDate
                        LocalDate paxValidFrom = new java.sql.Date(pax.getValidFrom().getTime()).toLocalDate();
                        LocalDate paxValidTo = new java.sql.Date(pax.getValidTo().getTime()).toLocalDate();

                        // Check if schedule dates fall within pax validity period
                        return !scheduleStartDate.isBefore(paxValidFrom) && !scheduleEndDate.isAfter(paxValidTo);
                    })
                    .findFirst()
                    .orElseThrow(() -> BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_PAX_NO_VALID));
        }

        // Get the original operator before update
        User originalOperator = existingSchedule.getOperator();

        // Update existing tour schedule with new values
        existingSchedule.setTour(tour);
        existingSchedule.setStartDate(startDate);
        existingSchedule.setEndDate(endDate);
        existingSchedule.setOperator(requestDTO.getOperatorId() != null ? operator : originalOperator);
        existingSchedule.setTourPax(tourPax);
        existingSchedule.setUpdatedAt(LocalDateTime.now());

        // Set tour schedule status to OPEN after update
        existingSchedule.setStatus(TourScheduleStatus.OPEN);

        TourSchedule updatedSchedule = tourScheduleRepository.save(existingSchedule);

        // Update tour status
        TourStatus currentStatus = tour.getTourStatus();
        tour.setTourStatus(TourStatus.PENDING);
        if (currentStatus == TourStatus.OPENED) {
            tour.setTourStatus(TourStatus.OPENED);
        }
        tourRepository.save(tour);

        return GeneralResponse.of(mapToResponseDTO(updatedSchedule), "Lịch trình tour đã được cập nhật thành công");
    }

    @Override
    public GeneralResponse<Object> cancelTourSchedule(Long scheduleId, User user) {
        // Find the existing tour schedule
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Lịch trình tour không tìm thấy"));

        // Check if the schedule is already cancelled or deleted
        if (schedule.getStatus() == TourScheduleStatus.CANCELLED) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, "Lịch trình tour đã bị hủy");
        }

        if (Boolean.TRUE.equals(schedule.getDeleted())) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, "Lịch trình tour đã bị xóa");
        }

        // Cancel and mark as deleted
        schedule.setStatus(TourScheduleStatus.CANCELLED);
        schedule.setDeleted(true);
        schedule.setUpdatedAt(LocalDateTime.now());

        tourScheduleRepository.save(schedule);

        return GeneralResponse.of(HttpStatus.OK, "Lịch trình tour đã được hủy thành công");
    }


    @Override
    public GeneralResponse<?> getTourScheduleSettlement(int page, int size, String keyword, String sortField, String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            List<TourScheduleStatus> statuses = new ArrayList<>();
            statuses.add(TourScheduleStatus.SETTLEMENT);
            statuses.add(TourScheduleStatus.ONGOING);
            statuses.add(TourScheduleStatus.COMPLETED);

            // Build search specification
            Specification<TourSchedule> spec = tourScheduleHelper.buildTourScheduleSearchSpecification(keyword, statuses);

            Page<TourSchedule> tourSchedulePage = tourScheduleRepository.findAll(spec, pageable);

            return tourScheduleHelper.buildPublicTourSchedulePagedResponse(tourSchedulePage);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> getSettlementDetails(Long tourScheduleId) {
        try {
            // Fetch tour schedule with all needed associations
            TourSchedule tourSchedule = tourScheduleRepository.findScheduleWithBookings(tourScheduleId)
                    .orElseThrow(() -> BusinessException.of("Lịch trình không tồn tại hoặc đã bị xóa"));

            TourSettlementResponseDTO dto = tourScheduleMapper.toDTO(tourSchedule);

            // Map to DTO
            List<TourBooking> bookings = tourBookingRepository.findBookingWithoutCustomersByScheduleId(tourScheduleId);

            List<TourBookingSettlementResponseDTO> bookingDTOS = bookings.stream().map(tourScheduleMapper::toSettlementDTO).toList();

            dto.setBookings(bookingDTOS);


            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> finishSettlement(Long tourScheduleId) {
        try {
            // Fetch tour schedule with all needed associations
            TourSchedule tourSchedule = tourScheduleRepository.findById(tourScheduleId).orElseThrow();
            tourSchedule.setStatus(TourScheduleStatus.COMPLETED);
            tourScheduleRepository.save(tourSchedule);


            List<TourBooking> tourBookings = tourBookingRepository.findByTourSchedule_Id(tourScheduleId);

            for (TourBooking tourBooking: tourBookings) {
                if(tourBooking.getStatus().toString().equalsIgnoreCase(TourBookingStatus.SUCCESS.toString())) {
                    tourBooking.setStatus(TourBookingStatus.COMPLETED);
                }
            }

            tourBookingRepository.saveAll(tourBookings);


            return GeneralResponse.of(TourScheduleStatus.COMPLETED);
        } catch (Exception ex) {
            throw BusinessException.of("Không hoàn thành tour", ex);
        }
    }

    @Override
    public GeneralResponse<?> getProviderByScheduleId(Long tourScheduleId) {
        try {

            List<ServiceProvider> providers = serviceProviderRepository.findServiceProviderByScheduleId(tourScheduleId);

            List<ServiceProviderSimpleDTO> providerSimpleDTOS = providers.stream().map(serviceProviderMapper::toServiceProviderSimpleDTO).toList();

            return GeneralResponse.of(providerSimpleDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    private TourScheduleBasicResponseDTO mapToResponseDTO(TourSchedule tourSchedule) {
        UserBasicDTO operatorDTO = null;
        if (tourSchedule.getOperator() != null) {
            operatorDTO = UserBasicDTO.builder()
                    .id(tourSchedule.getOperator().getId())
                    .username(tourSchedule.getOperator().getUsername())
                    .fullName(tourSchedule.getOperator().getFullName())
                    .email(tourSchedule.getOperator().getEmail())
                    .build();
        }
        // Map pax information
        TourPaxDTO paxInfoDTO = null;
        if (tourSchedule.getTourPax() != null) {
            TourPax pax = tourSchedule.getTourPax();
            paxInfoDTO = TourPaxDTO.builder()
                    .id(pax.getId())
                    .minPax(pax.getMinPax())
                    .maxPax(pax.getMaxPax())
                    .nettPricePerPax(pax.getNettPricePerPax())
                    .sellingPrice(pax.getSellingPrice())
                    .fixedCost(pax.getFixedCost())
                    .extraHotelCost(pax.getExtraHotelCost())
                    .validFrom(pax.getValidFrom())
                    .validTo(pax.getValidTo())
                    .build();
        }

        return TourScheduleBasicResponseDTO.builder()
                .id(tourSchedule.getId())
                .tourId(tourSchedule.getTour().getId())
                .tourName(tourSchedule.getTour().getName())
                .startDate(tourSchedule.getStartDate())
                .endDate(tourSchedule.getEndDate())
                .status(tourSchedule.getStatus().name())
                .operatorId(operatorDTO.getId())
                .operatorName(operatorDTO.getFullName())
                .paxInfo(paxInfoDTO)
                .build();
    }
    private boolean isWeekend(LocalDateTime date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
    private LocalDateTime getNextWeekday(LocalDateTime date) {
        LocalDateTime result = date;
        while (isWeekend(result)) {
            result = result.plusDays(1);
        }
        return result;
    }
}
