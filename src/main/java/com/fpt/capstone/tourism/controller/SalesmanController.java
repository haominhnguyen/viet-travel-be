package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PlanSaleResponseDTO;
import com.fpt.capstone.tourism.model.enums.PlanStatus;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.service.BookingService;
import com.fpt.capstone.tourism.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/salesman")
public class  SalesmanController {

    private static final Logger logger = Logger.getLogger(SalesmanController.class.getName());

    private final BookingService bookingService;
    private final PlanService planService;

    @GetMapping("/bookings/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>>> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(bookingService.getTourBookings(page, size, keyword, status, sortField, sortDirection));
    }


    @GetMapping("/tours/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<TourWithNumberBookingDTO>>>> getPublicTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "SIC") TourType tourType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tourStatus,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        TourStatus status = tourStatus != null && !tourStatus.isEmpty() ? TourStatus.valueOf(tourStatus.trim()) : null;
        return ResponseEntity.ok(bookingService.getTours(page, size, keyword, status, sortField, sortDirection, tourType));
    }


    @GetMapping("/tours/list-booking/{tourId}/{scheduleId}")
    public ResponseEntity<?> getTourListBooking(
            @PathVariable Long tourId,
            @PathVariable(required = false) Long scheduleId) {
        return ResponseEntity.ok(bookingService.getTourListBookings(tourId, scheduleId));
    }

    @GetMapping("/tours/list-booking/{tourId}")
    public ResponseEntity<?> getTourListBookingWithoutSchedule(
            @PathVariable Long tourId) {
        return ResponseEntity.ok(bookingService.getTourListBookings(tourId, null));
    }


    @GetMapping("/bookings/detail/{tourBookingId}")
    public ResponseEntity<?> getBookingsDetail(@PathVariable Long tourBookingId) {
        log.info("Start call api booking detail with ID: {}", tourBookingId);
        GeneralResponse<?> res = bookingService.saleViewBookingDetails(tourBookingId);
        log.info("End call api booking detail with ID: {}", tourBookingId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/bookings/services/{tourBookingId}")
    public ResponseEntity<?> getBookingsDetailServices(@PathVariable Long tourBookingId) {
        log.info("Start call api get service booking with ID: {}", tourBookingId);
        GeneralResponse<?> res = bookingService.getTourBookingServices(tourBookingId);
        log.info("End call api get service booking with ID: {}", tourBookingId);
        return ResponseEntity.ok(res);
    }


    @PostMapping("/bookings/services/update-quantity")
    public ResponseEntity<?> getBookingsDetailServices(@RequestBody UpdateServiceNotBookingSaleRequestDTO updateServiceNotBookingSaleRequestDTO) {
        return ResponseEntity.ok(bookingService.updateServiceQuantity(updateServiceNotBookingSaleRequestDTO));
    }


    @PostMapping("/bookings/services/cancel-service")
    public ResponseEntity<?> cancelService(@RequestBody Long tourBookingServiceId) {
        return ResponseEntity.ok(bookingService.cancelService(tourBookingServiceId));
    }


    @PostMapping("/bookings/services/success-service")
    public ResponseEntity<?> successService(@RequestBody Long tourBookingServiceId) {
        return ResponseEntity.ok(bookingService.successService(tourBookingServiceId));
    }


    @GetMapping("/bookings/customers/list/{tourBookingId}")
    public ResponseEntity<?> getBookingCustomers(@PathVariable Long tourBookingId) {
        return ResponseEntity.ok(bookingService.getTourBookingCustomers(tourBookingId));
    }


    @PostMapping("/bookings/customers/change-status")
    public ResponseEntity<?> updateCustomerStatus(@RequestBody Long tourBookingCustomerId) {
        return ResponseEntity.ok(bookingService.changeCustomerStatus(tourBookingCustomerId));
    }


    @PostMapping("/bookings/customers/update")
    public ResponseEntity<?> updateCustomers(@RequestBody UpdateCustomersRequestDTO updateCustomersRequestDTO) {
        return ResponseEntity.ok(bookingService.updateCustomers(updateCustomersRequestDTO));
    }


    @GetMapping("/bookings/create/tour/{tourId}/{scheduleId}")
    public ResponseEntity<?> updateCustomers(@PathVariable("tourId") Long tourId, @PathVariable("scheduleId") Long scheduleId) {
        return ResponseEntity.ok(bookingService.getTourDetails(tourId, scheduleId));
    }


    @GetMapping("/bookings/create/customers")
    public ResponseEntity<?> updateCustomers(@RequestParam(defaultValue = "", required = false) String customerName) {
        return ResponseEntity.ok(bookingService.getCustomersByName(customerName));
    }

    @PostMapping("/bookings/create")
    public ResponseEntity<?> createPublicBooking(@RequestBody CreatePublicBookingRequestDTO bookingRequestDTO) {
        return ResponseEntity.ok(bookingService.createBooking(bookingRequestDTO));
    }


    @PostMapping("/bookings/services/checking-available")
    public ResponseEntity<?> sendCheckingAvailable(@RequestBody CheckingServiceAvailableDTO dto) {
        return ResponseEntity.ok(bookingService.sendCheckingServiceAvailable(dto));
    }

    @PostMapping("/bookings/services/checking-available/all")
    public ResponseEntity<?> checking(@RequestBody Long bookingId) {
        return ResponseEntity.ok(bookingService.checkingAllService(bookingId));
    }


    @GetMapping("/tours/private/list")
    public ResponseEntity<?> sendCheckingAvailable(@RequestParam String name) {
        return ResponseEntity.ok(bookingService.getTourPrivateByName(name));
    }


    @GetMapping("/tours/private/details")
    public ResponseEntity<?> getTourContent(@RequestParam Long tourId) {
        return ResponseEntity.ok(bookingService.getTourContents(tourId));
    }


    @GetMapping("/tours/create/locations")
    public ResponseEntity<?> getLocations() {
        return ResponseEntity.ok(bookingService.getLocations());
    }


    @PostMapping("/tours/create")
    public ResponseEntity<?> createTourPrivate(@RequestBody CreateTourPrivateRequestDTO tour) {
        return ResponseEntity.ok(bookingService.createTourPrivate(tour));
    }

    @PostMapping("/tours/private/update")
    public ResponseEntity<?> updateTourPrivateContent(@RequestBody UpdateTourPrivateContentRequestDTO tour) {
        return ResponseEntity.ok(bookingService.updateTourPrivate(tour));
    }

    @PostMapping("/tours/private/change-status")
    public ResponseEntity<?> updateTourPrivateStatus(@RequestBody ChangeStatusTourPrivateRequestDTO tour) {
        return ResponseEntity.ok(bookingService.updateTourPrivateStatus(tour));
    }


    @GetMapping("/tour-days/service-categories/list/{tourId}")
    public ResponseEntity<?> getServiceCategoryWithTourDays(@PathVariable Long tourId) {
        return ResponseEntity.ok(bookingService.getServiceCategoryWithTourDays(tourId));
    }

    @GetMapping("/tours/locations/{tourId}")
    public ResponseEntity<?> getTourPrivateDetails(@PathVariable Long tourId) {
        return ResponseEntity.ok(bookingService.getTourLocations(tourId));
    }


    @GetMapping("/service-providers/list")
    public ResponseEntity<?> getServiceProviders(@RequestParam Long locationId, @RequestParam String categoryName) {
        return ResponseEntity.ok(bookingService.getServiceProviders(locationId, categoryName));
    }

    @GetMapping("/service-providers/service/list")
    public ResponseEntity<?> getServiceProviderServices(@RequestParam Long providerId, @RequestParam String categoryName) {
        return ResponseEntity.ok(bookingService.getServiceProviderServices(providerId, categoryName));
    }

    @PostMapping("/tours/services")
    public ResponseEntity<?> updateTourServices(@RequestBody List<TourPrivateServiceRequestDTO> dto) {
        return ResponseEntity.ok(bookingService.updateTourServices(dto));
    }

    @PostMapping("/bookings/cancel")
    public ResponseEntity<?> updateTourServices(@RequestBody CancelTourBookingRequestDTO dto) {
        return ResponseEntity.ok(bookingService.cancelBooking(dto));
    }


    @PostMapping("/tours/send-pricing")
    public ResponseEntity<?> sendPricing(@RequestBody Long tourId) {
        return ResponseEntity.ok(bookingService.sendPricing(tourId));
    }

    @PostMapping("/bookings/update-status")
    public ResponseEntity<?> updateBookingStatus(@RequestBody BookingStatusUpdateDTO dto) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(dto));
    }

    @PostMapping("/tours/send-operator")
    public ResponseEntity<?> sendOperator(@RequestBody SendOperatorDTO dto) {
        return ResponseEntity.ok(bookingService.sendOperator(dto));
    }


    @PostMapping("/bookings/take-booking")
    public ResponseEntity<?> takeBooking(@RequestBody TakeBookingRequestDTO dto) {
        return ResponseEntity.ok(bookingService.takeBooking(dto));
    }


    @PostMapping("/customers/create")
    public ResponseEntity<?> createCustomer(@RequestBody SaleCreateUserRequestDTO dto){
        return ResponseEntity.ok(bookingService.createCustomer(dto));
    }


    @PostMapping("/tours/forward/schedules")
    public ResponseEntity<?> createCustomer(@RequestBody ForwardScheduleRequestDTO dto){
        return ResponseEntity.ok(bookingService.getForwardSchedule(dto));
    }

    @PostMapping("/tours/forward")
    public ResponseEntity<?> forwardBooking(@RequestBody ForwardBookingRequestDTO dto){
        return ResponseEntity.ok(bookingService.forwardBooking(dto));
    }

    @PostMapping("/bookings/send-email")
    public ResponseEntity<?> getEmailContent(@RequestBody SendPriceRequestDTO dto){
        return ResponseEntity.ok(bookingService.getEmailContent(dto));
    }

    @PostMapping("/bookings/send-email/submit")
    public ResponseEntity<?> sendPrice(@RequestBody SendEmailPriceRequestDTO dto){
        return ResponseEntity.ok(bookingService.sendEmailPrice(dto));
    }

    @GetMapping("/plans/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<PlanSaleResponseDTO>>>> getPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) PlanStatus planStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(planService.getPlans(page, size, sortField, sortDirection, planStatus, keyword));
    }



    @PostMapping("/plans/update-status/{planId}")
    public ResponseEntity<?> updateStatus(@PathVariable(name = "planId") Long planId, @RequestBody String planStatus) {
        PlanStatus status = PlanStatus.valueOf(planStatus.trim());
        return ResponseEntity.ok(planService.updateStatus(planId, status));
    }



}
