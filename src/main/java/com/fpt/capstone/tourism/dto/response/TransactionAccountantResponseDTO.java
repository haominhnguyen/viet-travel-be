package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.CostAccountDTO;
import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class TransactionAccountantResponseDTO {
    private Long id;
    private TourBookingAccountantShortResponseDTO booking;
    private Double amount;
    private TransactionType category; // receipt hoặc payment
    private String paidBy; // Người trả tiền
    private String receivedBy; // Người nhận tiền
    private PaymentMethod paymentMethod;
    private String notes;
    private List<CostAccountDTO> costAccount;
    private TransactionStatus transactionStatus;
    private LocalDateTime createdAt;
}
