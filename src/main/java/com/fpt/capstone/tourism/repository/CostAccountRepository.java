package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.model.CostAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Arrays;
import java.util.List;

public interface CostAccountRepository extends JpaRepository<CostAccount, Long>, JpaSpecificationExecutor<CostAccount> {
    List<CostAccount> findByTransaction_Id(Long transactionId);
}
