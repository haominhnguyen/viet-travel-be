package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.TransactionHelper;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.repository.TransactionRepository;
import com.fpt.capstone.tourism.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionHelper transactionHelper;

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public GeneralResponse<?> getTransactions(int page, int size, String keyword, String sortField, String sortDirection, TransactionType transactionType) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<Transaction> spec = transactionHelper.buildTransactionPublicSearchSpecification(keyword, transactionType);

            Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

            return transactionHelper.buildPublicTransactionPagedResponse(transactionPage);
        } catch (Exception ex) {
            throw BusinessException.of("Get Data failed", ex);
        }
    }


}
