package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.ChangableServiceProviderDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceProviderDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import com.fpt.capstone.tourism.service.LocationService;
import com.fpt.capstone.tourism.service.ServiceProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/ceo/service-provider")
@RequiredArgsConstructor
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;
    private final LocationService locationService;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<ServiceProviderDTO>> create(@RequestBody ServiceProviderDTO serviceProviderDTO) {
        return ResponseEntity.ok(serviceProviderService.save(serviceProviderDTO));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<GeneralResponse<?>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(serviceProviderService.getServiceProviderById(id));
    }

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<ServiceProviderDTO>>>> getAllServiceProviders(@RequestParam(defaultValue = "0") int page,
                                                                                                       @RequestParam(defaultValue = "10") int size,
                                                                                                       @RequestParam(required = false) String keyword,
                                                                                                       @RequestParam(required = false) Boolean isDeleted,@RequestParam(defaultValue = "desc") String orderDate) {
        return ResponseEntity.ok(serviceProviderService.getAllServiceProviders(page, size, keyword, isDeleted, orderDate));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GeneralResponse<ServiceProviderDTO>> updateServiceProvider(@PathVariable Long id, @RequestBody ServiceProviderDTO serviceProviderDTO) {
        return ResponseEntity.ok(serviceProviderService.updateServiceProvider(id, serviceProviderDTO));
    }

    @DeleteMapping("/change-status/{id}")
    public ResponseEntity<GeneralResponse<ServiceProviderDTO>> deleteServiceProvider(@PathVariable Long id, @RequestParam boolean isDeleted) {
        return ResponseEntity.ok(serviceProviderService.deleteServiceProvider(id, isDeleted));
    }

    @GetMapping("/location/list-location")
    public ResponseEntity<GeneralResponse<?>> getListLocation(){
        return ResponseEntity.ok(locationService.getListLocation());
    }

}

