package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceDTO;
import com.fpt.capstone.tourism.dto.common.ServiceFullDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("service-provider/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<ServiceDTO>>>> getServices(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            Long providerId = getLoggedInServiceProviderId(userDetails);
            return ResponseEntity.ok(serviceService.getAllServices(
                    page, size, keyword, isDeleted, sortField, sortDirection, providerId));
        } catch (Exception e) {
            throw BusinessException.of(SERVICE_NOT_FOUND, e);
        }
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<GeneralResponse<ServiceFullDTO>> getServiceDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
            Long providerId = getLoggedInServiceProviderId(userDetails);
            return ResponseEntity.ok(serviceService.getServiceById(id, providerId));
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
