package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import jakarta.persistence.*;
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
    private String notes;
    private Long serviceId;
    private Integer quantity;
}
