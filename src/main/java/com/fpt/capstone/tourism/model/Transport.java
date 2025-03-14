package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;

@Entity
@Table(name = "transport")
public class Transport extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_capacity", nullable = false)
    private Integer seatCapacity; // Số ghế trên phương tiện

    @Column(name = "is_deleted")
    private Boolean deleted;

    @OneToOne
    @JoinColumn(name = "service_id", nullable = false, unique = true)
    private Service service; // Liên kết với bảng Service

}
