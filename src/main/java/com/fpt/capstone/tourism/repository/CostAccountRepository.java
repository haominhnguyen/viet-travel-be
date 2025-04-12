package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.common.RevenueChartDTO;
import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CostAccountRepository extends JpaRepository<CostAccount, Long>, JpaSpecificationExecutor<CostAccount> {
    List<CostAccount> findByTransaction_Id(Long transactionId);

    @Query("""
            SELECT new com.fpt.capstone.tourism.dto.common.RevenueChartDTO(
            EXTRACT(MONTH FROM ca.createdAt),
            EXTRACT(YEAR FROM ca.createdAt),
            CAST( SUM(ca.finalAmount) AS BIGDECIMAL )) 
            FROM CostAccount ca 
            JOIN Transaction tr ON ca.transaction.id = tr.id
            WHERE DATE(ca.createdAt) BETWEEN :startDate AND :endDate 
            AND tr.category IN :transactionTypes
            AND ca.status = :paid
            GROUP BY EXTRACT(YEAR FROM ca.createdAt), EXTRACT(MONTH FROM ca.createdAt) 
            ORDER BY EXTRACT(YEAR FROM ca.createdAt) DESC, EXTRACT(MONTH FROM ca.createdAt) DESC
            """)
    List<RevenueChartDTO> getRevenueByMonth(LocalDate startDate, LocalDate endDate, List<TransactionType> transactionTypes, CostAccountStatus paid);
}
