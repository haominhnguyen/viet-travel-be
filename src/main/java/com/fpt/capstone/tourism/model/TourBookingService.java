package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tour_booking_service")
public class TourBookingService extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tour_booking_id")
    private TourBooking tourBooking;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

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

    private String status; //(e.g., Pending, Approved, Rejected, Wait Confirmed).

}
