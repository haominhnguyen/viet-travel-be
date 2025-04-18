package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.request.CreateTransactionRequestDTO;
import com.fpt.capstone.tourism.dto.request.UpdateTransactionRequestDTO;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.service.BookingService;
import com.fpt.capstone.tourism.service.TourScheduleService;
import com.fpt.capstone.tourism.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accountant")
public class AccountantController {

    private final TransactionService transactionService;
    private final TourScheduleService tourScheduleService;
//    private final BookingService bookingService;


    @GetMapping("/transactions/list")
    public ResponseEntity<?> getVoucherList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String transactionStatus,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam List<TransactionType> transactionTypes
    ) {
        return ResponseEntity.ok(transactionService.getTransactions(page, size, keyword, sortField, sortDirection, transactionTypes, transactionStatus));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionDetails(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionDetails(id));
    }

    @PostMapping("/transactions/update")
    public ResponseEntity<?> updateTransaction(@RequestBody UpdateTransactionRequestDTO dto) {
        return ResponseEntity.ok(transactionService.updateTransaction(dto));
    }

    @GetMapping("/transactions/bookings/list")
    public ResponseEntity<?> updateTransaction(@RequestParam String keyword) {
        return ResponseEntity.ok(transactionService.getBookingByBookingCode(keyword));
    }

    @PostMapping("/transactions/create")
    public ResponseEntity<?> createTransaction(@RequestBody CreateTransactionRequestDTO dto) {
        return ResponseEntity.ok(transactionService.createTransaction(dto));
    }

    @GetMapping("/transactions/providers")
    public ResponseEntity<?> getBookingProvider(@RequestParam Long bookingId) {
        return ResponseEntity.ok(transactionService.getBookingProvider(bookingId));
    }


    @GetMapping("/tour-schedules/list-settlements")
    public ResponseEntity<?> getTourScheduleSettlement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(tourScheduleService.getTourScheduleSettlement(page, size, keyword, sortField, sortDirection));
    }


    @GetMapping("/settlements/details")
    public ResponseEntity<?> getSettlementDetails(@RequestParam Long tourScheduleId) {
        return ResponseEntity.ok(tourScheduleService.getSettlementDetails(tourScheduleId));
    }


    @PostMapping("/settlements/finish")
    public ResponseEntity<?> finishSettlement(@RequestBody Long tourScheduleId) {
        return ResponseEntity.ok(tourScheduleService.finishSettlement(tourScheduleId));
    }


    @GetMapping("/settlements/service-provider/{tourScheduleId}")
    public ResponseEntity<?> listProviderBySchedule(@PathVariable(name = "tourScheduleId") Long tourScheduleId) {
        return ResponseEntity.ok(tourScheduleService.getProviderByScheduleId(tourScheduleId));
    }
}
