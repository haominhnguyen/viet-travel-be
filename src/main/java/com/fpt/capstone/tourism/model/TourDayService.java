package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
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

//    @ManyToOne
//    @JoinColumn(name = "tour_pax_id")
//    private TourPax tourPax;
    @Column(name = "deleted")
    private Boolean deleted = false;
    @OneToMany(mappedBy = "tourDayService", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicePaxPricing> paxPricings = new ArrayList<>();
}
