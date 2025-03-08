package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.repository.TransactionRepository;
import com.fpt.capstone.tourism.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
