package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.common.OperatorTransactionDTO;
import com.fpt.capstone.tourism.dto.common.TourOperationLogDTO;
import com.fpt.capstone.tourism.model.TourOperationLog;
import com.fpt.capstone.tourism.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper extends EntityMapper<OperatorTransactionDTO, Transaction>{
}
