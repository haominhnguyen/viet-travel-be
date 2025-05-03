package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionHelper {
    Specification<Transaction> buildTransactionPublicSearchSpecification(String keyword, List<TransactionType> transactionTypes, String transactionStatus);
    GeneralResponse<?> buildPublicTransactionPagedResponse(Page<Transaction> transactionPage);
    Double calculateTransportFeePerPerson(Long scheduleId, Long serviceId);
}
