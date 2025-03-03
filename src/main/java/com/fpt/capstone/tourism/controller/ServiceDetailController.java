package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.ServiceDetailRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.ServiceDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fpt.capstone.tourism.constants.Constants.Message.SERVICE_PROVIDER_NOT_FOUND;
import static com.fpt.capstone.tourism.constants.Constants.Message.USER_NOT_AUTHENTICATED;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.USER_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("service-provider/services/{serviceId}/details")
@Slf4j
public class ServiceDetailController {

    private final ServiceDetailService serviceDetailService;
    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<ServiceDetailResponseDTO>> createServiceDetail(
            @PathVariable Long serviceId,
            @RequestBody ServiceDetailRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long providerId = getLoggedInServiceProviderId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceDetailService.createServiceDetail(serviceId, providerId, requestDTO));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<List<ServiceDetailResponseDTO>>> getAllServiceDetails(
            @PathVariable Long serviceId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long providerId = getLoggedInServiceProviderId(userDetails);
        return ResponseEntity.ok(serviceDetailService.getAllServiceDetails(serviceId, providerId));
    }

    @GetMapping("/{detailId}")
    public ResponseEntity<GeneralResponse<ServiceDetailResponseDTO>> getServiceDetailById(
            @PathVariable Long serviceId,
            @PathVariable Long detailId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long providerId = getLoggedInServiceProviderId(userDetails);
        return ResponseEntity.ok(serviceDetailService.getServiceDetailById(serviceId, detailId, providerId));
    }

    @PutMapping("/update/{detailId}")
    public ResponseEntity<GeneralResponse<ServiceDetailResponseDTO>> updateServiceDetail(
            @PathVariable Long serviceId,
            @PathVariable Long detailId,
            @RequestBody ServiceDetailRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long providerId = getLoggedInServiceProviderId(userDetails);
        return ResponseEntity.ok(serviceDetailService.updateServiceDetail(serviceId, detailId, providerId, requestDTO));
    }

    @PostMapping("/change-status/{detailId}")
    public ResponseEntity<GeneralResponse<Boolean>> changeServiceDetailStatus(
            @PathVariable Long serviceId,
            @PathVariable Long detailId,
            @RequestBody boolean isDeleted,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long providerId = getLoggedInServiceProviderId(userDetails);
        return ResponseEntity.ok(serviceDetailService.changeServiceDetailStatus(serviceId, detailId, isDeleted,providerId));
    }

    private Long getLoggedInServiceProviderId(UserDetails userDetails) {
        if (userDetails == null) {
            throw BusinessException.of(USER_NOT_AUTHENTICATED);
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.of(USER_NOT_FOUND));

        ServiceProvider serviceProvider = serviceProviderRepository.findByUserId(user.getId())
                .orElseThrow(() -> BusinessException.of(SERVICE_PROVIDER_NOT_FOUND));

        return serviceProvider.getId();
    }
}

