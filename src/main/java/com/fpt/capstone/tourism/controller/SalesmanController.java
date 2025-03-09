package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/salesman")
public class SalesmanController {


    private final BookingService bookingService;

    @GetMapping("/bookings/list")
    public ResponseEntity<GeneralResponse<?>> getBookingsList() {
        return ResponseEntity.ok(bookingService.viewListBooking());
    }

}
