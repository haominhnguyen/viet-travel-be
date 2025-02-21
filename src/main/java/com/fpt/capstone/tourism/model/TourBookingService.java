package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


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


    @Column(name = "is_deleted")
    private Boolean deleted;


    private String status;


    private String reason;

}
