package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "tour_operation_log")
public class TourOperationLog extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tour_schedule_id")
    private TourSchedule tourSchedule;

    private String content;
    private String action;

    @Column(name = "is_deleted")
    private Boolean deleted;
}
