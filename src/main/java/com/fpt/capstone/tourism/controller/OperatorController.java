package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.service.OperatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/tour-detail/{scheduleId}/create-operation-log")
    public ResponseEntity<GeneralResponse<TourOperationLogDTO>> createOperationLog(@PathVariable Long scheduleId,
                                                                                   @RequestBody TourOperationLogRequestDTO logRequestDTO) {
        return ResponseEntity.ok(operatorService.createOperationLog(scheduleId, logRequestDTO));
    }

    @DeleteMapping("/tour-detail/operation-log/change-status/{logId}")
    public ResponseEntity<GeneralResponse<TourOperationLogDTO>> deleteOperationLog(@PathVariable Long logId) {
        return ResponseEntity.ok(operatorService.deleteOperationLog(logId));
    }

    @PostMapping("/tour-detail/{scheduleId}/assign-tour-guide")
    public ResponseEntity<GeneralResponse<AssignTourGuideRequestDTO>> assignTourGuide(@PathVariable Long scheduleId,
                                                                                   @RequestBody AssignTourGuideRequestDTO requestDTO) {
        return ResponseEntity.ok(operatorService.assignTourGuide(scheduleId, requestDTO));
    }

    @GetMapping("/tour-detail/{scheduleId}/list-available-tour-guide")
    public ResponseEntity<GeneralResponse<List<UserResponseDTO>>> getListAvailableTourGuide(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getListAvailableTourGuide(scheduleId));
    }

    @GetMapping("/tour-detail/{scheduleId}/list-transaction")
    public ResponseEntity<GeneralResponse<List<OperatorTransactionDTO>>> getListTransaction(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getListTransaction(scheduleId));
    }

    @GetMapping("/tour-detail/{scheduleId}/summary")
    public ResponseEntity<GeneralResponse<?>> getTourSummary(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getTourSummary(scheduleId));
    }

    @GetMapping("/tour-detail/{scheduleId}/list-service")
    public ResponseEntity<GeneralResponse<OperatorServiceListDTO>> getListService(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getListService(scheduleId));
    }

    @GetMapping("/choose-service-to-pay/{serviceId}")
    public ResponseEntity<GeneralResponse<PublicServiceProviderDTO>> chooseServiceToPay(@PathVariable Long serviceId) {
        return ResponseEntity.ok(operatorService.chooseServiceToPay(serviceId));
    }

    @PostMapping("/pay-service")
    public ResponseEntity<GeneralResponse<OperatorTransactionDTO>> payService(@RequestBody PayServiceRequestDTO requestDTO) {
        return ResponseEntity.ok(operatorService.payService(requestDTO));
    }

    @GetMapping("/tour-service/list-location-and-service-category/{tourScheduleId}")
    public ResponseEntity<GeneralResponse<?>> getListLocationAndServiceCategory(@PathVariable Long tourScheduleId) {
        return ResponseEntity.ok(operatorService.getListLocationAndServiceCategory(tourScheduleId));
    }

    @GetMapping("/tour-service/{scheduleId}/list-booking")
    public ResponseEntity<GeneralResponse<?>> getListBookingForAddService(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getListBookingForAddService(scheduleId));
    }

    @GetMapping("/tour-service/{locationId}/list-service-provider/{serviceCategoryId}")
    public ResponseEntity<GeneralResponse<Map<Long, String>>> getListServiceProviderByLocationIdAndServiceCategoryId(@PathVariable Long locationId,
                                                                                                  @PathVariable Long serviceCategoryId) {
        return ResponseEntity.ok(operatorService.getListServiceProviderByLocationIdAndServiceCategoryId(locationId, serviceCategoryId));
    }

    @GetMapping("/tour-service/{serviceProviderId}/list-service/{serviceCategoryId}")
    public ResponseEntity<GeneralResponse<List<ServiceSimpleDTO>>> getListServiceOfAProvider(@PathVariable Long serviceProviderId,
                                                                                             @PathVariable Long serviceCategoryId) {
        return ResponseEntity.ok(operatorService.getListServiceByServiceProviderId(serviceProviderId, serviceCategoryId));
    }

    @GetMapping("/tour-service/{serviceId}/service-detail")
    public ResponseEntity<GeneralResponse<?>> getServiceDetail(@PathVariable Long serviceId) {
        return ResponseEntity.ok(operatorService.getServiceDetail(serviceId));
    }

    @PostMapping("/add-service")
    public ResponseEntity<GeneralResponse<?>> addService(@RequestBody AddServiceRequestDTO requestDTO) {
        return ResponseEntity.ok(operatorService.addService(requestDTO));
    }

    @PostMapping("/preview-mail")
    public ResponseEntity<GeneralResponse<?>> previewMail(@RequestBody PreviewMailDTO previewMailDTO) {
        return ResponseEntity.ok(operatorService.previewMail(previewMailDTO));
    }

    @PostMapping("/send-mail-to-provider")
    public ResponseEntity<GeneralResponse<?>> sendMailToProvider(@RequestBody MailServiceDTO mailServiceDTO) {
        return ResponseEntity.ok(operatorService.sendMailToProvider(mailServiceDTO));
    }

    @GetMapping("/list-service-request")
    public ResponseEntity<GeneralResponse<?>> getListServiceRequest(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(operatorService.getListServiceRequest(page, size));
    }

    @GetMapping("/change-service-request-detail/{tourBookingServiceId}")
    public ResponseEntity<GeneralResponse<?>> getChangeServiceRequestDetail(@PathVariable("tourBookingServiceId") Long tourBookingServiceId) {
        return ResponseEntity.ok(operatorService.getChangeServiceRequestDetail(tourBookingServiceId));
    }

    @PutMapping("/reject-service-request/{tourBookingServiceId}")
    public ResponseEntity<GeneralResponse<?>> rejectServiceRequest(@PathVariable Long tourBookingServiceId) {
        return ResponseEntity.ok(operatorService.rejectServiceRequest(tourBookingServiceId));
    }

    @PutMapping("/approve-service-request/{tourBookingServiceId}")
    public ResponseEntity<GeneralResponse<?>> approveServiceRequest(@PathVariable Long tourBookingServiceId) {
        return ResponseEntity.ok(operatorService.approveServiceRequest(tourBookingServiceId));
    }

    @PutMapping("/update-service-quantity")
    public ResponseEntity<GeneralResponse<?>> updateServiceQuantity(@RequestBody ServiceQuantityUpdateDTO requestDTO) {
        return ResponseEntity.ok(operatorService.updateServiceQuantity(requestDTO));
    }
    @PutMapping("/cancel-service/{tourBookingServiceId}")
    public ResponseEntity<GeneralResponse<?>> cancelService(@PathVariable("tourBookingServiceId") Long tourBookingServiceId) {
        return ResponseEntity.ok(operatorService.cancelService(tourBookingServiceId));
    }

    @PutMapping("/tours/send-accountant/{tourScheduleId}")
    public ResponseEntity<?> sendAccountant(@PathVariable Long tourScheduleId) {
        return ResponseEntity.ok(operatorService.sendAccountant(tourScheduleId));
    }

    @GetMapping("/list-tour-private")
    public ResponseEntity<GeneralResponse<PagingDTO<List<OperatorTourDTO>>>> getListPrivateTour(@RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int size,
                                                                                         @RequestParam(required = false) String keyword,
                                                                                         @RequestParam(value = "status", required = false) String status,
                                                                                         @RequestParam(defaultValue = "desc") String orderDate) {
        return ResponseEntity.ok(operatorService.getListTourPrivate(page, size, keyword, status, orderDate));
    }

    @GetMapping("/tour-detail/{scheduleId}/list-tour-day")
    public ResponseEntity<?> getListTourDayOfSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(operatorService.getListTourDayOfSchedule(scheduleId));
    }

}
