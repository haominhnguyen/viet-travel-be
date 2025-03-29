package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.CostAccountDTO;
import com.fpt.capstone.tourism.dto.common.OperatorTransactionDTO;
import com.fpt.capstone.tourism.dto.common.TourOperationLogDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingAccountantShortResponseDTO;
import com.fpt.capstone.tourism.dto.response.TransactionAccountantResponseDTO;
import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TourOperationLog;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper extends EntityMapper<OperatorTransactionDTO, Transaction>{
    @Mapping(target = "paymentStatus", expression = "java(getPaymentStatus(transaction))")
    OperatorTransactionDTO toDTO(Transaction transaction);

    default String getPaymentStatus(Transaction transaction) {
        // Lấy danh sách CostAccount có status == PAID
        List<CostAccount> paidCostAccounts = transaction.getCostAccount().stream()
                .filter(cost -> cost.getStatus() == CostAccountStatus.PAID)
                .toList();

        double totalPaid = 0.0;
        if(!paidCostAccounts.isEmpty()){
            // Tính tổng tiền đã trả
            totalPaid = paidCostAccounts.stream()
                    .mapToDouble(CostAccount::getFinalAmount)
                    .sum();
        }

        // Xác định trạng thái thanh toán
        if (totalPaid >= transaction.getAmount()) {
            return "PAID";
        } else if (totalPaid > 0) {
            return "PARTIALLY_PAID";
        } else {
            return "UNPAID";
        }
    }

    @Mapping(source = "createdAt", target = "createdAt")
    TransactionAccountantResponseDTO toTransactionAccountantResponseDTO(Transaction transaction);
    CostAccountDTO toCostAccountDTO(CostAccountDTO costAccount);

    TourBookingAccountantShortResponseDTO toTourBookingAccountantShortResponseDTO(TourBooking tourBooking);
}
