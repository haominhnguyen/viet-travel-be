package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class TourDayService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tour_day_id")
    private TourDay tourDay;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    private Integer quantity;

    @Column(name = "selling_price")
    private Double sellingPrice;


}
