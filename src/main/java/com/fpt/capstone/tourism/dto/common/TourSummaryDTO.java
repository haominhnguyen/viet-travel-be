package com.fpt.capstone.tourism.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TourSummaryDTO {
    private Long tourScheduleId;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal receiptedAmount;                   //Số tiền công ty đã thu
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal remainingReceiptAmount;           //Số tiền còn phải thu
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal collectionAmount;                //Số tiền HDV đã thu hộ
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal totalReceiptAmount;             //Tổng tiền phải thu
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal paymentAmount;                 //Số tiền công ty đã chi
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal remainingPaymentAmount;       //Số tiền còn phải chi
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal advanceAmount;               //Số tiền HDV đã chi hộ
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal totalPaymentAmount;         //Tổng tiền phải chi
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal estimatedPaymentAmount;    //Số tiền ước tính phải chi
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal estimateReceiptAmount;    //Số tiền ước tính thu được
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal estimateProfitAmount;    //Lợi nhuận ước tính
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal actualProfitAmount;     //Lợi nhuận thực tế
}
