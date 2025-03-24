package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.ServiceDetailDTO;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.service.TourDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour/{tourId}/discount")
public class TourDiscountController {
    private final TourDiscountService tourDiscountService;

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<TourServiceListDTO>> getTourServicesList(
            @PathVariable Long tourId,
            @RequestParam(required = false) Integer paxCount) {
        return ResponseEntity.ok(tourDiscountService.getTourServicesList(tourId, paxCount));
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<GeneralResponse<ServiceByCategoryDTO>> getServiceDetail(
            @PathVariable Long tourId,
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(tourDiscountService.getServiceDetail(tourId, serviceId));
    }

    @GetMapping("/provider/{providerId}/location/{locationId}")
    public ResponseEntity<GeneralResponse<ServiceProviderServicesDTO>> getServiceProviderServices(
            @PathVariable Long tourId,
            @PathVariable Long providerId,
            @PathVariable Long locationId) {
        return ResponseEntity.ok(tourDiscountService.getServiceProviderServices(providerId, locationId));
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<GeneralResponse<ServiceByCategoryDTO>> updateServiceDetail(
            @PathVariable Long tourId,
            @PathVariable Long serviceId,
            @RequestBody ServiceUpdateRequestDTO request) {
        return ResponseEntity.ok(tourDiscountService.updateServiceDetail(tourId, serviceId, request));
    }

    @GetMapping("/providers")
    public ResponseEntity<GeneralResponse<ServiceProviderOptionsDTO>> getServiceProviderOptions(
            @PathVariable Long tourId,
            @RequestParam Long locationId,
            @RequestParam String categoryName) {
        return ResponseEntity.ok(tourDiscountService.getServiceProviderOptions(locationId, categoryName));
    }
}
