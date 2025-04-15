package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayServiceRequestDTO {
    private Long bookingId;
    @Min(value = 1, message = "Số tiền chi phải lớn hơn 0")
    private Double amount;
    private String paidBy;
    private String receivedBy;
    private PaymentMethod paymentMethod;
    @Pattern(regexp = "PAYMENT|ADVANCED", message = "TransactionType must be PAYMENT or ADVANCED")
    private TransactionType transactionType;
    private String notes;
    private Long serviceId;
    @Min(value = 1, message = "Số lượng dịch vụ phải lớn hơn 0")
    private Integer quantity;
    private Long tourDayId;
}
