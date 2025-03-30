package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.UpdateTransactionRequestDTO;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accountant")
public class AccountantController {

    private final TransactionService transactionService;


    @GetMapping("/transactions/list")
    public ResponseEntity<?> getVoucherList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "RECEIPT") TransactionType transactionType
    ) {
        return ResponseEntity.ok(transactionService.getTransactions(page, size, keyword, sortField, sortDirection, transactionType));
    }



    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionDetails(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionDetails(id));
    }

    @PostMapping("/transactions/update")
    public ResponseEntity<?> updateTransaction(@RequestBody UpdateTransactionRequestDTO dto) {
        return ResponseEntity.ok(transactionService.updateTransaction(dto));
    }


}
