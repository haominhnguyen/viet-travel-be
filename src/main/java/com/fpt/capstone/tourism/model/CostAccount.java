package com.fpt.capstone.tourism.model;


import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cost_account")
public class CostAccount extends  BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    @ToString.Exclude
    private Transaction transaction;

    private String content;


    private Double amount; // Đơn giá

    private int discount;

    private int quantity;

    @Column(name = "final_amount")
    private Double finalAmount;

    @Enumerated(EnumType.STRING)
    private CostAccountStatus status;


}
