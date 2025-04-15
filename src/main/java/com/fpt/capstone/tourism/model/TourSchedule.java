package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.TourScheduleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "tour_schedule")
public class TourSchedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pax_id", nullable = false)
    private TourPax tourPax;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TourScheduleStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_guide_id")
    private User tourGuide;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(name = "meeting_location")
    private String meetingLocation;

    @Column(name = "departure_time", columnDefinition = "TIME")
    private LocalTime departureTime;

    @OneToMany(mappedBy = "tourSchedule", fetch = FetchType.LAZY)
    private Set<TourOperationLog> operationLogs;

    @OneToMany(mappedBy = "tourSchedule", fetch = FetchType.LAZY)
    private List<TourBooking> bookings;
}
