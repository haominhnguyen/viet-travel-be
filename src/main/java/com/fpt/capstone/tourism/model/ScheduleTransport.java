package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;

@Entity
@Table(name = "schedule_transport")
public class ScheduleTransport extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private TourSchedule tourSchedule; // Liên kết với lịch trình Tour

    @ManyToOne
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport; // Liên kết với phương tiện di chuyển

    @Column(name = "is_deleted")
    private Boolean deleted;
}

