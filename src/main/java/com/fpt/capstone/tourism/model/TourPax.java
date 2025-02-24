package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "tour_pax")
public class TourPax extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "fixed_cost", nullable = false)
    private Double fixedCost;

    @Column(name = "min_pax", nullable = false)
    private int maxPax;

    @Column(name = "max_pax", nullable = false)
    private int minPax;

    @Column(name = "extra_hotel_cost", nullable = false)
    private Double extraHotelCost;

    @Column(name = "nett_price_per_pax", nullable = false)
    private Double nettPricePerPax;

    @Column(name = "selling_price")
    private Double sellingPrice;

    @Temporal(TemporalType.DATE)
    @Column(name = "valid_from", nullable = false)
    private Date validFrom;

    @Temporal(TemporalType.DATE)
    @Column(name = "valid_to", nullable = false)
    private Date validTo;
}
