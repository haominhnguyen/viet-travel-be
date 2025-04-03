package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/salesman")
public class SalesmanController {


    private final BookingService bookingService;

    @GetMapping("/bookings/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>>> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(bookingService.getTourBookings(page, size, keyword, isDeleted, sortField, sortDirection));
    }


    @GetMapping("/tours/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<TourWithNumberBookingDTO>>>> getPublicTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "20") TourType tourType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(bookingService.getTours(page, size, keyword, isDeleted, sortField, sortDirection, tourType));
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
        return ResponseEntity.ok(bookingService.saleViewBookingDetails(tourBookingId));
    }

    @GetMapping("/bookings/services/{tourBookingId}")
    public ResponseEntity<?> getBookingsDetailServices(@PathVariable Long tourBookingId) {
        return ResponseEntity.ok(bookingService.getTourBookingServices(tourBookingId));
    }


    @PostMapping("/bookings/services/update-quantity")
    public ResponseEntity<?> getBookingsDetailServices(@RequestBody UpdateServiceNotBookingSaleRequestDTO updateServiceNotBookingSaleRequestDTO) {
        return ResponseEntity.ok(bookingService.updateServiceQuantity(updateServiceNotBookingSaleRequestDTO));
    }


    @PostMapping("/bookings/services/cancel-service")
    public ResponseEntity<?> cancelService(@RequestBody Long tourBookingServiceId) {
        return ResponseEntity.ok(bookingService.cancelService(tourBookingServiceId));
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
    public ResponseEntity<?> sendCheckingAvailable(@RequestBody Long tourBookingServiceId) {
        return ResponseEntity.ok(bookingService.sendCheckingServiceAvailable(tourBookingServiceId));
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
        return ResponseEntity.ok(bookingService.cancelTour(dto));
    }

}
