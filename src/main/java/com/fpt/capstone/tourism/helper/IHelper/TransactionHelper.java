package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

public interface TransactionHelper {
    Specification<Transaction> buildTransactionPublicSearchSpecification(String keyword, TransactionType transactionType);
    GeneralResponse<?> buildPublicTransactionPagedResponse(Page<Transaction> transactionPage);
}
