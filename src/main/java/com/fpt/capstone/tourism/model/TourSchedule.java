package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Date;
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

    private Date date;

    @ManyToOne
    @JoinColumn(name = "pax_id", nullable = false)
    private TourPax tourPax;

    @Column(name = "is_deleted")
    private Boolean deleted;

    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_guide_id")
    private User tourGuide;

    @Column(name = "meeting_location")
    private String meetingLocation;

    @Column(name = "departure_time", columnDefinition = "TIME")
    private LocalTime departureTime;


    @OneToMany(mappedBy = "tourSchedule")
    private Set<TourOperationLog> operationLogs;



}
