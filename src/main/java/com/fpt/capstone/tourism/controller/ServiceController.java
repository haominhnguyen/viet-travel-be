package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.ServiceCategoryService;
import com.fpt.capstone.tourism.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.*;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.USER_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("service-provider/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceCategoryService serviceCategoryService;

    @GetMapping("/list-categories")
    public ResponseEntity<GeneralResponse<List<ServiceCategoryDTO>>> getAllServiceCategories() {
        return ResponseEntity.ok(serviceCategoryService.getAllServiceCategories());
    }

    @GetMapping("/list-service-request")
    public ResponseEntity<GeneralResponse<?>> getListServiceRequest(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) TourBookingServiceStatus status,
                                                                    @RequestParam(defaultValue = "desc") String orderDate) {
        return ResponseEntity.ok(serviceService.getListServiceRequest(page, size, keyword, status, orderDate));
    }

    @GetMapping("/service-request-detail/{tourBookingServiceId}")
    public ResponseEntity<GeneralResponse<?>> getServiceRequestDetail(@PathVariable Long tourBookingServiceId) {
        return ResponseEntity.ok(serviceService.getServiceRequestDetail(tourBookingServiceId));
    }

    @PutMapping("/approve/{tourBookingServiceId}")
    public ResponseEntity<GeneralResponse<?>> approveService(@PathVariable Long tourBookingServiceId) {
        return ResponseEntity.ok(serviceService.approveService(tourBookingServiceId));
    }

    @PutMapping("/reject/{tourBookingServiceId}")
    public ResponseEntity<GeneralResponse<?>> rejectService(@PathVariable Long tourBookingServiceId) {
        return ResponseEntity.ok(serviceService.rejectService(tourBookingServiceId));
    }



//    @GetMapping("/list")
//    public ResponseEntity<GeneralResponse<PagingDTO<List<ServiceBaseDTO>>>> getServices(
//            @AuthenticationPrincipal UserDetails userDetails,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) Boolean isDeleted,
//            @RequestParam(defaultValue = "id") String sortField,
//            @RequestParam(defaultValue = "desc") String sortDirection) {
//        try {
//            Long providerId = getLoggedInServiceProviderId(userDetails);
//            return ResponseEntity.ok(serviceService.getAllServices(
//                    page, size, keyword, isDeleted, sortField, sortDirection, providerId));
//        } catch (Exception e) {
//            throw BusinessException.of(SERVICE_NOT_FOUND, e);
//        }
//    }
//
//
//    @GetMapping("/tour-day-services/{serviceId}")
//    public ResponseEntity<GeneralResponse<List<TourDayServiceDTO>>> getTourDayServicesByService(
//            @PathVariable Long serviceId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long providerId = getLoggedInServiceProviderId(userDetails);
//        return ResponseEntity.ok(serviceService.getTourDayServicesByServiceId(serviceId, providerId));
//    }
//
//    @GetMapping("/details/{serviceId}")
//    public ResponseEntity<GeneralResponse<Object>> getServiceDetailsByService(
//            @PathVariable Long serviceId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long providerId = getLoggedInServiceProviderId(userDetails);
//        return ResponseEntity.ok(serviceService.getServiceDetailsByServiceId(serviceId, providerId));
//    }
//
//    @PostMapping("/create")
//    public ResponseEntity<GeneralResponse<ServiceResponseDTO>> createService(
//            @Valid @RequestBody ServiceRequestDTO requestDTO,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        try {
//            Long providerId = getLoggedInServiceProviderId(userDetails);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(serviceService.createService(requestDTO, providerId));
//        } catch (BusinessException be) {
//            throw be;
//        } catch (Exception e) {
//            throw BusinessException.of(CREATE_SERVICE_FAIL, e);
//        }
//    }
//
//    @PutMapping("/update/{serviceId}")
//    public ResponseEntity<GeneralResponse<ServiceResponseDTO>> updateService(
//            @PathVariable Long serviceId,
//            @Valid @RequestBody ServiceRequestDTO requestDTO,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        try {
//            Long providerId = getLoggedInServiceProviderId(userDetails);
//            return ResponseEntity.ok(serviceService.updateService(serviceId, requestDTO, providerId));
//        } catch (BusinessException be) {
//            throw be;
//        } catch (Exception e) {
//            throw BusinessException.of(UPDATE_SERVICE_FAIL, e);
//        }
//    }
//
//    @PostMapping("/change-status/{serviceId}")
//    public ResponseEntity<GeneralResponse<ServiceResponseDTO>> changeServiceStatus(
//            @PathVariable Long serviceId,
//            @RequestBody Boolean isDeleted,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        try {
//            Long providerId = getLoggedInServiceProviderId(userDetails);
//            return ResponseEntity.ok(serviceService.changeServiceStatus(serviceId, isDeleted, providerId));
//        } catch (BusinessException be) {
//            throw be;
//        } catch (Exception e) {
//            throw BusinessException.of(CHANGE_SERVICE_STATUS_FAIL, e);
//        }
//    }

//    private Long getLoggedInServiceProviderId(UserDetails userDetails) {
//        if (userDetails == null) {
//            throw BusinessException.of(USER_NOT_AUTHENTICATED);
//        }
//        User user = userRepository.findByUsername(userDetails.getUsername())
//                .orElseThrow(() -> BusinessException.of(USER_NOT_FOUND));
//        ServiceProvider serviceProvider = serviceProviderRepository.findByUserId(user.getId())
//                .orElseThrow(() -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND));
//        return serviceProvider.getId();
//    }
}
