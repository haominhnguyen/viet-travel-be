package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PayServiceRequestDTO {
    private Long bookingId;
    private Double amount;
    private String paidBy;
    private String receivedBy;
    private PaymentMethod paymentMethod;
    @Pattern(regexp = "PAYMENT|ADVANCED", message = "TransactionType must be PAYMENT or ADVANCED")
    private TransactionType transactionType;
    private String notes;
    private Long serviceId;
    private Integer quantity;
}
