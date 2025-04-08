package com.fpt.capstone.tourism.model;


import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "tour_booking")
public class TourBooking extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int seats;

    private String note;

    @Column(name = "booking_code", unique = true)
    private String bookingCode;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private User sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    @ToString.Exclude
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    @ToString.Exclude
    private TourSchedule tourSchedule;

    @OneToMany(mappedBy = "tourBooking", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TourBookingCustomer> customers;

    @Enumerated(EnumType.STRING)
    private TourBookingStatus status;

    @Enumerated(EnumType.STRING)
    private TourBookingCategory tourBookingCategory;

    @Column(name = "selling_price")
    private Double sellingPrice;

    @Column(name = "extra_hotel_cost")
    private Double extraHotelCost;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String reason;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;


    @Column(name = "payment_url", columnDefinition = "text")
    private String paymentUrl;

    @OneToMany(mappedBy = "booking")
    @ToString.Exclude
    private List<TourBookingService> tourBookingServices;

    @OneToMany(mappedBy = "booking")
    @ToString.Exclude
    private List<Transaction> transactions;




}
