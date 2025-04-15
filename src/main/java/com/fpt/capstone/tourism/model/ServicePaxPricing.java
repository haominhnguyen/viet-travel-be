package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "service_pax_pricing")
public class ServicePaxPricing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tour_day_service_id", nullable = false)
    private TourDayService tourDayService;

    @ManyToOne
    @JoinColumn(name = "tour_pax_id", nullable = false)
    private TourPax tourPax;

    @Column(name = "selling_price")
    private Double sellingPrice;

    @Column(name = "deleted")
    private Boolean deleted = false;
}