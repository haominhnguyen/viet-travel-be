package com.fpt.capstone.tourism.model;


import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private TourSchedule tourSchedule;

    @OneToMany(mappedBy = "tourBooking")
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

    @ManyToOne
    @JoinColumn(name = "schedule_service_id")
    private TourBookingService tourBookingService;

    @OneToMany(mappedBy = "booking")
    private List<Transaction> transactions;




}
