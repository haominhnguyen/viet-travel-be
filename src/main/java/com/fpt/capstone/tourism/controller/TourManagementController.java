package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourDayServiceResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.LocationService;
import com.fpt.capstone.tourism.service.TagService;
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

import static com.fpt.capstone.tourism.constants.Constants.Message.USER_NOT_AUTHENTICATED;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.USER_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour")
public class TourManagementController {
    private final TourService tourService;
    private final TourDayServiceI tourDayServiceI;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final TagService tagService;

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

    @GetMapping("/detail-schedule/{id}")
    public ResponseEntity<GeneralResponse<TourDetailDTO>> getTourWithActiveSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourWithActiveSchedule(id));
    }

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<TourResponseDTO>> createTour(
            @Valid @RequestBody TourRequestDTO tourRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getLoggedInUser(userDetails);
        GeneralResponse<TourResponseDTO> response = tourService.createTour(tourRequestDTO, user);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/send-for-approval/{tourId}")
    public ResponseEntity<GeneralResponse<TourResponseDTO>> sendTourForApproval(
            @PathVariable Long tourId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getLoggedInUser(userDetails);
        GeneralResponse<TourResponseDTO> response = tourService.sendTourForApproval(tourId, user);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/open-tour/{tourId}")
    public ResponseEntity<GeneralResponse<TourResponseDTO>> openTour(
            @PathVariable Long tourId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getLoggedInUser(userDetails);
        GeneralResponse<TourResponseDTO> response = tourService.openTour(tourId, user);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/change-to-pending-pricing/{tourId}")
    public ResponseEntity<GeneralResponse<TourResponseDTO>> changeToPendingPricing(
            @PathVariable Long tourId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getLoggedInUser(userDetails);
        GeneralResponse<TourResponseDTO> response = tourService.changeToPendingPricing(tourId, user);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    private User getLoggedInUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw BusinessException.of(HttpStatus.UNAUTHORIZED, USER_NOT_AUTHENTICATED);
        }

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
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

    @GetMapping("/list-location")
    public ResponseEntity<GeneralResponse<PagingDTO<List<LocationDTO>>>> getAll(@RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "10") int size,
                                                                                @RequestParam(required = false) String keyword,
                                                                                @RequestParam(required = false) Boolean isDeleted,
                                                                                @RequestParam(defaultValue = "desc") String orderDate) {
        return ResponseEntity.ok(locationService.getAllLocation(page, size, keyword, isDeleted, orderDate));
    }

    @GetMapping("/list-tag")
    public ResponseEntity<GeneralResponse<List<TagDTO>>> getTags() {
        return ResponseEntity.ok(tagService.findAll());
    }



}
