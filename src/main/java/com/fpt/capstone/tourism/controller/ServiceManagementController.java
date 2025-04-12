package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ChangeStatusRequestDTO;
import com.fpt.capstone.tourism.dto.request.ServiceRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.service.ServiceCategoryService;
import com.fpt.capstone.tourism.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("ceo/services")
public class ServiceManagementController {

    private final ServiceService serviceService;
    private final ServiceCategoryService serviceCategoryService;

    @GetMapping("/list-categories")
    public ResponseEntity<GeneralResponse<List<ServiceCategoryDTO>>> getAllServiceCategories() {
        return ResponseEntity.ok(serviceCategoryService.getAllServiceCategories());
    }

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<ServiceBaseDTO>>>> getServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) Long providerId,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            return ResponseEntity.ok(serviceService.getAllServices(
                    page, size, keyword, isDeleted, sortField, sortDirection, providerId));
        } catch (Exception e) {
            throw BusinessException.of(SERVICE_NOT_FOUND, e);
        }
    }
    @GetMapping("/tour-day-services/{serviceId}")
    public ResponseEntity<GeneralResponse<List<TourDayServiceDTO>>> getTourDayServicesByService(
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceService.getTourDayServicesByServiceId(serviceId, null));
    }
    @GetMapping("/details/{serviceId}")
    public ResponseEntity<GeneralResponse<Object>> getServiceDetailsByService(
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceService.getServiceDetailsByServiceId(serviceId, null));
    }
    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<ServiceResponseDTO>> createService(
            @Valid @RequestBody ServiceRequestDTO requestDTO) {
        try {
            if (requestDTO.getProviderId() == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Service provider ID is required");
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(serviceService.createService(requestDTO, requestDTO.getProviderId()));
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw BusinessException.of(CREATE_SERVICE_FAIL, e);
        }
    }
    @PutMapping("/update/{serviceId}")
    public ResponseEntity<GeneralResponse<ServiceResponseDTO>> updateService(
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceRequestDTO requestDTO) {
        try {
            if (requestDTO.getProviderId() == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Service provider ID is required");
            }
            return ResponseEntity.ok(serviceService.updateService(serviceId, requestDTO, requestDTO.getProviderId()));
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw BusinessException.of(UPDATE_SERVICE_FAIL, e);
        }
    }
    @PostMapping("/change-status/{serviceId}")
    public ResponseEntity<GeneralResponse<ServiceResponseDTO>> changeServiceStatus(
            @PathVariable Long serviceId,
            @RequestBody ChangeStatusRequestDTO statusRequest) {
        try {
            if (statusRequest.getProviderId() == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Service provider ID is required");
            }
            return ResponseEntity.ok(serviceService.changeServiceStatus(
                    serviceId, statusRequest.getIsDeleted(), statusRequest.getProviderId()));
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw BusinessException.of(CHANGE_SERVICE_STATUS_FAIL, e);
        }
    }
}

