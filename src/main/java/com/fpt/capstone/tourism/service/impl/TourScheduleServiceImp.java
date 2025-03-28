package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.EndDateOption;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.OperatorAvailabilityDTO;
import com.fpt.capstone.tourism.dto.request.TourScheduleRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourScheduleBasicResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.TourScheduleStatus;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.TourScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
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

    public GeneralResponse<List<EndDateOption>> calculatePossibleEndDates(Long tourId, LocalDateTime startDate) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

        List<EndDateOption> endDateOptions = new ArrayList<>();

        // Basic calculation: add number of days to start date
        LocalDateTime basicEndDate = startDate.plusDays(tour.getNumberDays());
        endDateOptions.add(new EndDateOption(
                basicEndDate,
                String.format("Standard option: %d days, %d nights",
                        tour.getNumberDays(), tour.getNumberNights()),
                true
        ));

        // Alternative 1: if tour spans weekend, offer option to extend to next weekday
        if (isWeekend(basicEndDate)) {
            LocalDateTime nextWeekdayEnd = getNextWeekday(basicEndDate);
            endDateOptions.add(new EndDateOption(
                    nextWeekdayEnd,
                    String.format("Extended weekend option: %d days, %d nights",
                            ChronoUnit.DAYS.between(startDate, nextWeekdayEnd),
                            ChronoUnit.DAYS.between(startDate, nextWeekdayEnd) - 1),
                    false
            ));
        }

        // Alternative 2: Offer a +1 day option for flexibility
        LocalDateTime extendedEndDate = basicEndDate.plusDays(1);
        endDateOptions.add(new EndDateOption(
                extendedEndDate,
                String.format("Extended option: %d days, %d nights",
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
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Tour not found"));

        // Get the selected operator
        User operator = userRepository.findById(requestDTO.getOperatorId())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, OPERATOR_NOT_FOUND));

        // Validate that the user is an operator
        boolean isOperator = operator.getUserRoles().stream()
                .anyMatch(userRole -> !userRole.getDeleted() &&
                        userRole.getRole().getRoleName().equals("OPERATOR"));

        if (!isOperator) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, USER_NOT_OPERATOR);
        }

        // Check operator availability
        int activeToursCount = tourScheduleRepository.countActiveToursForOperator(
                operator.getId(), requestDTO.getStartDate(), requestDTO.getEndDate());

        if (activeToursCount > 3) {
            throw BusinessException.of(
                    HttpStatus.BAD_REQUEST,
                    OPERATOR_OVERBOOKED
            );
        }

        // Get default TourPax (or create if not exists)
        TourPax defaultPax = tourPaxRepository.findByTourAndIsDefault(tour)
                .orElseGet(() -> createDefaultTourPax(tour));

        // Create new tour schedule
        TourSchedule tourSchedule = new TourSchedule();
        tourSchedule.setTour(tour);
        tourSchedule.setStartDate(requestDTO.getStartDate());
        tourSchedule.setEndDate(requestDTO.getEndDate());
        tourSchedule.setOperator(operator);
        tourSchedule.setTourPax(defaultPax);
        tourSchedule.setStatus(TourScheduleStatus.DRAFT);
        tourSchedule.setDeleted(false);

        tourSchedule = tourScheduleRepository.save(tourSchedule);

        return GeneralResponse.of(mapToResponseDTO(tourSchedule), SCHEDULE_CREATED_SUCCESS);
    }

    private TourPax createDefaultTourPax(Tour tour) {
        TourPax defaultPax = new TourPax();
        defaultPax.setTour(tour);
        defaultPax.setMinPax(1);
        defaultPax.setMaxPax(10);
        return tourPaxRepository.save(defaultPax);
    }

    private TourScheduleBasicResponseDTO mapToResponseDTO(TourSchedule tourSchedule) {
        return TourScheduleBasicResponseDTO.builder()
                .id(tourSchedule.getId())
                .tourId(tourSchedule.getTour().getId())
                .tourName(tourSchedule.getTour().getName())
                .startDate(tourSchedule.getStartDate())
                .endDate(tourSchedule.getEndDate())
                .operatorId(tourSchedule.getOperator().getId())
                .operatorName(tourSchedule.getOperator().getFullName())
                .status(tourSchedule.getStatus().name())
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
