package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.EndDateOption;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.OperatorAvailabilityDTO;
import com.fpt.capstone.tourism.dto.request.TourScheduleRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourScheduleBasicResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourScheduleResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.TourScheduleService;
import com.fpt.capstone.tourism.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour/schedule")
public class TourScheduleController {
    private final TourService tourService;
    private final TourScheduleService tourScheduleService;
    private final UserRepository userRepository;

    @GetMapping("/calculate-end-dates/{tourId}")
    public ResponseEntity<GeneralResponse<List<EndDateOption>>> calculateEndDates(
            @PathVariable Long tourId,
            @RequestParam LocalDateTime startDate) {
        return ResponseEntity.ok(tourScheduleService.calculatePossibleEndDates(tourId, startDate));
    }

    @GetMapping("/available-operators")
    public ResponseEntity<GeneralResponse<List<OperatorAvailabilityDTO>>> getAvailableOperators(
            @RequestParam Long tourId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(tourScheduleService.findAvailableOperators(tourId, startDate, endDate));
    }

    @PostMapping("/set")
    public ResponseEntity<GeneralResponse<TourScheduleBasicResponseDTO>> setTourSchedule(
            @Valid @RequestBody TourScheduleRequestDTO scheduleRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getLoggedInUser(userDetails);
        GeneralResponse<TourScheduleBasicResponseDTO> response =
                tourScheduleService.setTourSchedule(scheduleRequestDTO, user);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/update")
    public ResponseEntity<GeneralResponse<TourScheduleBasicResponseDTO>> updateTourSchedule(
            @Valid @RequestBody TourScheduleRequestDTO scheduleRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getLoggedInUser(userDetails);
        GeneralResponse<TourScheduleBasicResponseDTO> response =
                tourScheduleService.updateTourSchedule(scheduleRequestDTO, user);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/cancel/{scheduleId}")
    public ResponseEntity<GeneralResponse<Object>> cancelTourSchedule(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getLoggedInUser(userDetails);
        GeneralResponse<Object> response = tourScheduleService.cancelTourSchedule(scheduleId, user);
        return ResponseEntity.status(response.getCode()).body(response);
    }


    private User getLoggedInUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw BusinessException.of(HttpStatus.UNAUTHORIZED, "Người dùng không có quyền truy cập");
        }

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }
}
