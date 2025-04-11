package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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
}
