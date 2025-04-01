package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourWithNumberBookingDTO;
import com.fpt.capstone.tourism.dto.request.CreateTransactionRequestDTO;
import com.fpt.capstone.tourism.dto.request.UpdateTransactionRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.TourType;

import java.util.List;

public interface TransactionService {
    Transaction createTransaction(Transaction transaction);

    GeneralResponse<?> getTransactions(int page, int size, String keyword, String sortField, String sortDirection, TransactionType transactionType);

    GeneralResponse<?> getTransactionDetails(Long id);


    GeneralResponse<?> updateTransaction(UpdateTransactionRequestDTO dto);

    GeneralResponse<?> getBookingByBookingCode(String keyword);


    GeneralResponse<?> createTransaction(CreateTransactionRequestDTO dto);
}
