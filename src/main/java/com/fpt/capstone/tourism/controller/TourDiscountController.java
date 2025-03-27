package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceCreateRequestDTO;
import com.fpt.capstone.tourism.dto.response.ActivityDetailResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailDTO;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.service.ActivityCategoryService;
import com.fpt.capstone.tourism.service.ActivityService;
import com.fpt.capstone.tourism.service.LocationService;
import com.fpt.capstone.tourism.service.TourDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour/{tourId}/discount")
public class TourDiscountController {
    private final TourDiscountService tourDiscountService;
    private final ActivityService activityService;
    private final LocationService locationService;
    private final ActivityCategoryService activityCategoryService;

    //Service
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

    @GetMapping("/services/{serviceId}/days")
    public ResponseEntity<GeneralResponse<List<Integer>>> getDayNumbersByServiceAndTour(
            @PathVariable Long tourId,
            @PathVariable Long serviceId) {
        GeneralResponse<List<Integer>> response = tourDiscountService.getDayNumbersByServiceAndTour(tourId, serviceId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{tourId}/provider/{providerId}/category/{categoryName}/location/{locationId}")
    public ResponseEntity<GeneralResponse<ServiceProviderServicesDTO>> getServicesByProviderAndCategory(
            @PathVariable Long tourId,
            @PathVariable Long providerId,
            @PathVariable String categoryName,
            @PathVariable Long locationId) {
        return ResponseEntity.ok(tourDiscountService.getServicesByProviderAndCategory(providerId, categoryName, locationId));
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

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<ServiceByCategoryDTO>> createServiceDetail(
            @PathVariable Long tourId,
            @RequestBody ServiceCreateRequestDTO request) {
        return ResponseEntity.ok(tourDiscountService.createServiceDetail(tourId, request));
    }
//    @DeleteMapping("/{serviceId}")
//    public ResponseEntity<GeneralResponse<Void>> changeServiceStatus(
//            @PathVariable Long tourId,
//            @PathVariable Long serviceId,
//            @RequestParam(required = false, defaultValue = "true") Boolean delete) {
//        return ResponseEntity.ok(tourDiscountService.changeServiceStatus(tourId, serviceId, delete));
//    }

    //Activity
    @GetMapping("/activity")
    public ResponseEntity<GeneralResponse<List<ActivityListDTO>>> getActivityList(
            @PathVariable Long tourId) {
        return ResponseEntity.ok(activityService.getActivityList(tourId));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<GeneralResponse<ActivityDetailResponseDTO>> getActivityDetail(
            @PathVariable Long tourId,
            @PathVariable Long activityId) {
        return ResponseEntity.ok(activityService.getActivityDetail(tourId, activityId));
    }

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

    @GetMapping("/activity-categories")
    public ResponseEntity<GeneralResponse<List<ActivityCategoryDTO>>> getAllActivityCategories() {
        return ResponseEntity.ok(activityCategoryService.getAllActivityCategories());
    }

    @GetMapping("/locations/{locationId}/activity-categories/{categoryId}/activities")
    public ResponseEntity<GeneralResponse<List<ActivityBasicDTO>>> getActivitiesByLocationAndCategory(
            @PathVariable Long locationId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(activityService.getActivitiesByLocationAndCategory(locationId, categoryId));
    }

//    @PostMapping("/activity/create")
//    public ResponseEntity<GeneralResponse<ActivityDetailDTO>> createActivity(
//            @PathVariable Long tourId,
//            @RequestBody ActivityCreateUpdateRequestDTO request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(activityService.createActivity(tourId, request));
//    }
//
//    @PutMapping("/activity/update/{activityId}")
//    public ResponseEntity<GeneralResponse<ActivityDetailDTO>> updateActivity(
//            @PathVariable Long tourId,
//            @PathVariable Long activityId,
//            @RequestBody ActivityCreateUpdateRequestDTO request) {
//        return ResponseEntity.ok(activityService.updateActivity(tourId, activityId, request));
//    }

    //Tour Guide

}
