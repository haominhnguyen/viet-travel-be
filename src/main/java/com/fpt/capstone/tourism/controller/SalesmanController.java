package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.service.BookingService;
import lombok.RequiredArgsConstructor;
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



}
