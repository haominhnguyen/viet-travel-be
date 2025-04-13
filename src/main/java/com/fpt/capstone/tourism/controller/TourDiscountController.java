package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceCreateRequestDTO;
import com.fpt.capstone.tourism.dto.response.ActivityDetailResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailDTO;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.LocationService;
import com.fpt.capstone.tourism.service.TourDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fpt.capstone.tourism.constants.Constants.Message.TICKET;
import static com.fpt.capstone.tourism.constants.Constants.Message.USER_NOT_AUTHENTICATED;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.USER_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour/{tourId}/discount")
public class TourDiscountController {
    private final TourDiscountService tourDiscountService;
    private final LocationService locationService;
    private final UserRepository userRepository;
    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<TourServiceListDTO>> getTourServicesList(
            @PathVariable Long tourId,
            @RequestParam(required = false) Integer paxCount) {
        return ResponseEntity.ok(tourDiscountService.getTourServicesList(tourId, paxCount));
    }

    @GetMapping("/day/{dayNumber}/service/{serviceId}")
    public ResponseEntity<GeneralResponse<ServiceByCategoryDTO>> getServiceDetailByDayAndService(
            @PathVariable Long tourId,
            @PathVariable Integer dayNumber,
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(tourDiscountService.getServiceDetailByDayAndService(tourId, dayNumber, serviceId));
    }

    @GetMapping("/services/{serviceId}/days")
    public ResponseEntity<GeneralResponse<List<Integer>>> getDayNumbersByServiceAndTour(
            @PathVariable Long tourId,
            @PathVariable Long serviceId) {
        GeneralResponse<List<Integer>> response = tourDiscountService.getDayNumbersByServiceAndTour(tourId, serviceId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/provider/{providerId}/category/{categoryName}/location/{locationId}")
    public ResponseEntity<GeneralResponse<ServiceProviderServicesDTO>> getServicesByProviderAndCategory(
            @PathVariable Long tourId,
            @PathVariable Long providerId,
            @PathVariable String categoryName,
            @PathVariable Long locationId) {
        if (TICKET.equals(categoryName)) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST,
                    "Flight Ticket is not valid here");
        }
        return ResponseEntity.ok(tourDiscountService.getServicesByProviderAndCategory(providerId, categoryName, locationId));
    }

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<ServiceByCategoryDTO>> createServiceDetail(
            @PathVariable Long tourId,
            @RequestBody ServiceCreateRequestDTO request) {
        return ResponseEntity.ok(tourDiscountService.createServiceDetail(tourId, request));
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<GeneralResponse<ServiceByCategoryDTO>> updateServiceDetail(
            @PathVariable Long tourId,
            @PathVariable Long serviceId,
            @RequestBody ServiceUpdateRequestDTO request) {
        return ResponseEntity.ok(tourDiscountService.updateServiceDetail(tourId, serviceId, request));
    }

    @DeleteMapping("/remove/services/{serviceId}")
    public ResponseEntity<GeneralResponse<Void>> removeServiceFromTour(
            @PathVariable Long tourId,
            @PathVariable Long serviceId,
            @RequestParam(required = true) Integer dayNumber,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getLoggedInUser(userDetails);
        GeneralResponse<Void> response = tourDiscountService.removeServiceFromTour(tourId, serviceId, dayNumber);
        return ResponseEntity.status(response.getCode()).body(response);
    }
    private User getLoggedInUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw BusinessException.of(HttpStatus.UNAUTHORIZED, USER_NOT_AUTHENTICATED);
        }

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }

    @GetMapping("/providers")
    public ResponseEntity<GeneralResponse<ServiceProviderOptionsDTO>> getServiceProviderOptions(
            @PathVariable Long tourId,
            @RequestParam Long locationId,
            @RequestParam String categoryName) {
        // Check if category is TICKET and return error
        if (TICKET.equals(categoryName)) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST,
                    "Flight Ticket is not valid here");
        }
        return ResponseEntity.ok(tourDiscountService.getServiceProviderOptions(locationId, categoryName));
    }

    @GetMapping("/ticket-providers")
    public ResponseEntity<GeneralResponse<ServiceProviderOptionsDTO>> getTicketProviders(
            @PathVariable Long tourId) {
        return ResponseEntity.ok(tourDiscountService.getTicketProviders());
    }

    @GetMapping("/ticket-provider/{providerId}")
    public ResponseEntity<GeneralResponse<ServiceProviderServicesDTO>> getServicesByTicketProvider(
            @PathVariable Long tourId,
            @PathVariable Long providerId) {
        return ResponseEntity.ok(tourDiscountService.getServicesByTicketProvider(providerId));
    }

//    @DeleteMapping("/{serviceId}")
//    public ResponseEntity<GeneralResponse<Void>> changeServiceStatus(
//            @PathVariable Long tourId,
//            @PathVariable Long serviceId,
//            @RequestParam(required = false, defaultValue = "true") Boolean delete) {
//        return ResponseEntity.ok(tourDiscountService.changeServiceStatus(tourId, serviceId, delete));
//    }

    @GetMapping("/list-location")
    public ResponseEntity<GeneralResponse<PagingDTO<List<LocationDTO>>>> getLocationsByTourId(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(defaultValue = "desc") String orderDate) {
        return ResponseEntity.ok(locationService.getLocationsByTourId(tourId, page, size, keyword, isDeleted, orderDate));
    }

}
