package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourProcessDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.TourBookingService;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.service.BookingService;
import com.fpt.capstone.tourism.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/head-of-business")
@RequiredArgsConstructor
public class RefundController {

    private final TransactionService transactionService;
    private final BookingService bookingService;

    @GetMapping("/refund-request/list")
    public ResponseEntity<?> getAllRefundRequest(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Boolean isDeleted,
                                                 @RequestParam(defaultValue = "id") String sortField,
                                                 @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(bookingService.getAllRefundRequest(page, size, keyword,isDeleted, sortField, sortDirection));
    }

    @GetMapping("/refund-request/detail/{tourBookingId}")
    public ResponseEntity<?> getDetailRefundRequest(@PathVariable Long tourBookingId) {
        return ResponseEntity.ok(bookingService.getDetailRefundRequest(tourBookingId));
    }
    @PutMapping("/refund-request/approve/{tourBookingId}")
    public ResponseEntity<?> approveRefundRequest(@PathVariable Long tourBookingId) {
        return ResponseEntity.ok(bookingService.approveRefundRequest(tourBookingId));
    }
    @PutMapping("/refund-request/reject/{tourBookingId}")
    public ResponseEntity<?> rejectRefundRequest(@PathVariable Long tourBookingId) {
        return ResponseEntity.ok(bookingService.rejectRefundRequest(tourBookingId));
    }
}
