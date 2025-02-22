package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private TourBookingService booking;

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
    private String paymentMethod;

    @Column(length = 500)
    private String notes;
}
