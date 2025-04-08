package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourBookingHistoryDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/booking-history")
public class BookingHistoryController {
    private final BookingService bookingService;
    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<TourBookingHistoryDTO>>>> viewListBookingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
            @RequestParam(defaultValue = "desc") String orderDate
    ){
        return ResponseEntity.ok(bookingService.viewListBookingHistory(page, size, keyword, paymentStatus, orderDate));
    }

    @GetMapping("/detail/{bookingCode}")
    public ResponseEntity<GeneralResponse<?>> viewBookingHistoryDetail(@PathVariable("bookingCode") String bookingCode){
        return ResponseEntity.ok(bookingService.getTourBookingDetails(bookingCode));
    }

    @PutMapping("/cancel-booking/{bookingCode}")
    public ResponseEntity<GeneralResponse<?>> cancelBooking(@PathVariable("bookingCode") String bookingCode){
        return ResponseEntity.ok(bookingService.cancelBooking(bookingCode));
    }
}
