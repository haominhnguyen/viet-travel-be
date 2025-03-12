package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.TourScheduleServiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tour_schedule_service")
public class TourScheduleService extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "tourScheduleService")
    private List<TourBooking> bookings;

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

    @Enumerated(EnumType.STRING)
    private TourScheduleServiceStatus status; //(e.g., Pending, Approved, Rejected, Wait Confirmed).

}
