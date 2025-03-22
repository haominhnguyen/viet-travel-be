package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private TourBooking booking;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType category; // receipt hoặc payment

    @Column(name = "paid_by", nullable = false)
    private String paidBy; // Người trả tiền

    @Column(name = "received_by", nullable = false)
    private String receivedBy; // Người nhận tiền

    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "transaction")
    private List<CostAccount> costAccount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;
}
