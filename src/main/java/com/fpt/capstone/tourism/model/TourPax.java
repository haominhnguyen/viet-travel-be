package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.TourStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    private int minPax;

    @Column(name = "max_pax", nullable = false)
    private int maxPax;

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

    @Column(name = "is_deleted")
    private Boolean deleted;
}
