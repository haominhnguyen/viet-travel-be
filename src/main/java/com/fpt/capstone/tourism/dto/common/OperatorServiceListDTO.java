package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OperatorServiceListDTO {
    List<OperatorServiceDTO> services;
    private Integer totalNumOfService;
    private Double paidAmount;
    private Double remainingAmount;
    private Double totalAmount;
}
