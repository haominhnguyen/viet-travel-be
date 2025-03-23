package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "tour_booking_service")
public class TourBookingService extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_booking_id")
    @ToString.Exclude
    private TourBooking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    @ToString.Exclude
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_day_id")
    @ToString.Exclude
    private TourDay tourDay;

    @Column(name = "current_quantity")
    private int currentQuantity;

    @Column(name = "requested_quantity")
    private int requestedQuantity;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    private TourBookingServiceStatus status; //(e.g., Pending, Approved, Rejected).

}
