package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.LocationRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourOperationLogRequestDTO;
import com.fpt.capstone.tourism.dto.response.OperatorTourDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.service.OperatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/operator")
public class OperatorController {

    private final OperatorService operatorService;
    @GetMapping("/list-tour")
    public ResponseEntity<GeneralResponse<PagingDTO<List<OperatorTourDTO>>>> getListTour(@RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int size,
                                                                                         @RequestParam(required = false) String keyword,
                                                                                         @RequestParam(value = "status", required = false) String status,
                                                                                         @RequestParam(defaultValue = "desc") String orderDate) {
        return ResponseEntity.ok(operatorService.getListTour(page, size, keyword, status, orderDate));
    }

    @PutMapping("/operate-tour/{id}")
    public ResponseEntity<GeneralResponse<OperatorTourDTO>> operateTour(@PathVariable Long id) {
        return ResponseEntity.ok(operatorService.operateTour(id));
    }

    @GetMapping("/tour-detail/{scheduleId}")
    public ResponseEntity<GeneralResponse<OperatorTourDetailDTO>> getTourDetail(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getTourDetail(scheduleId));
    }

    @GetMapping("/tour-detail/{scheduleId}/list-customer")
    public ResponseEntity<GeneralResponse<List<OperatorTourCustomerDTO>>> getListCustomerOfTourDetail(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getListCustomerOfTourDetail(scheduleId));
    }

    @GetMapping("/tour-detail/{scheduleId}/list-booking")
    public ResponseEntity<GeneralResponse<List<OperatorTourBookingDTO>>> getListBookingOfTourDetail(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getListBookingOfTourDetail(scheduleId));
    }

    @GetMapping("/tour-detail/{scheduleId}/list-operation-log")
    public ResponseEntity<GeneralResponse<List<TourOperationLogDTO>>> getListLogOfTourDetail(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getListOperationLogOfTourDetail(scheduleId));
    }

    @GetMapping("/tour-detail/{scheduleId}/create-operation-log")
    public ResponseEntity<GeneralResponse<TourOperationLogDTO>> createOperationLog(@PathVariable Long scheduleId,
                                                                                   @RequestBody TourOperationLogRequestDTO logRequestDTO) {
        return ResponseEntity.ok(operatorService.createOperationLog(scheduleId, logRequestDTO));
    }

    @DeleteMapping("/tour-detail/operation-log/change-status/{logId}")
    public ResponseEntity<GeneralResponse<TourOperationLogDTO>> deleteOperationLog(@PathVariable Long logId) {
        return ResponseEntity.ok(operatorService.deleteOperationLog(logId));
    }
}
