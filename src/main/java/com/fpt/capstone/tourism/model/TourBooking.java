package com.fpt.capstone.tourism.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
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

    @Column(name = "booking_date")
    private Date bookingDate;

    private int seats;

    private String note;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @OneToOne
    @JoinColumn(name = "schedule_id")
    private TourSchedule tourSchedule;

    @OneToMany(mappedBy = "tourBooking")
    private List<TourBookingCustomer> customers;


    @OneToMany(mappedBy = "tourBooking")
    private List<TourScheduleService> tourScheduleServices;

    private String status;


    private String reason;

}
