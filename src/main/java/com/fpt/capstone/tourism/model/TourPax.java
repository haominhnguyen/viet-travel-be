package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.TourStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @OneToMany(mappedBy = "tourPax", fetch = FetchType.LAZY)
    private List<TourSchedule> tourSchedule;

    @Column(name = "fixed_cost")
    private Double fixedCost;

    @Column(name = "min_pax", nullable = false)
    private int minPax;

    @Column(name = "max_pax", nullable = false)
    private int maxPax;

    @Column(name = "extra_hotel_cost")
    private Double extraHotelCost;

    @Column(name = "nett_price_per_pax")
    private Double nettPricePerPax;

    @Column(name = "selling_price")
    private Double sellingPrice;

    @Temporal(TemporalType.DATE)
    @Column(name = "valid_from")
    private Date validFrom;

    @Temporal(TemporalType.DATE)
    @Column(name = "valid_to")
    private Date validTo;

    @Column(name = "is_deleted")
    private Boolean deleted;
}
