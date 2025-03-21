package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourDayServiceResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.TourDayServiceI;
import com.fpt.capstone.tourism.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour")
public class TourManagementController {
    private final TourService tourService;
    private final TourDayServiceI tourDayServiceI;
    private final UserRepository userRepository;

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<TourBasicDTO>>>> getAllTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) Boolean isOpened,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        Pageable pageable = PageRequest.of(page, size,
                sortDirection.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());
        return ResponseEntity.ok(tourService.getAllTours(keyword, isDeleted, isOpened,pageable));
    }

        @GetMapping("/detail/{id}")
        public ResponseEntity<GeneralResponse<TourDetailDTO>> getTourById(@PathVariable Long id) {
            return ResponseEntity.ok(tourService.getTourDetail(id));
    }

    @GetMapping("/{tourId}/list-tour-days")
    public ResponseEntity<GeneralResponse<List<TourDayFullDTO>>> getTourDaysByTourId(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourDayServiceI.getTourDayDetail(tourId));
    }

    @PostMapping("/{tourId}/tour-days/create")
    public ResponseEntity<GeneralResponse<TourDayFullDTO>> createTourDayDetail(
            @RequestBody TourDayCreateRequestDTO tourDayCreateRequestDTO) {
        return ResponseEntity.ok(tourDayServiceI.createTourDay(tourDayCreateRequestDTO));
    }

    @PutMapping("/{tourId}/tour-days/update/{tourDayId}")
    public ResponseEntity<GeneralResponse<TourDayFullDTO>> updateTourDayDetail(
            @PathVariable Long tourDayId,
            @RequestBody TourDayUpdateRequestDTO tourDayUpdateDTO) {
        return ResponseEntity.ok(tourDayServiceI.updateTourDay(tourDayId, tourDayUpdateDTO));
    }

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<TourResponseDTO>> createTour(
            @Valid @RequestBody TourRequestDTO tourRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getLoggedInUser(userDetails);
        GeneralResponse<TourResponseDTO> response = tourService.createTour(tourRequestDTO, user);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    private User getLoggedInUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw BusinessException.of(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "User not found"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GeneralResponse<TourResponseDTO>> updateTour(
            @PathVariable Long id,
            @RequestBody TourRequestDTO tourRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = getLoggedInUser(userDetails);
        GeneralResponse<TourResponseDTO> response = tourService.updateTour(id, tourRequestDTO,user);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{tourId}/tour-days/{tourDayId}/services/add")
    public ResponseEntity<GeneralResponse<TourDayServiceResponseDTO>> addServiceToTourDay(
            @PathVariable Long tourId,
            @PathVariable Long tourDayId,
            @RequestBody TourDayServiceRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getLoggedInUser(userDetails);
        requestDTO.setId(tourDayId);
        return ResponseEntity.ok(tourDayServiceI.addServiceToTourDay(requestDTO, user));
    }
}
