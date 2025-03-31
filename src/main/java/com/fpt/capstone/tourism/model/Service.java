package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@ToString(exclude = {"tourDayServices", "serviceDetails"})
//@EqualsAndHashCode(callSuper = true, exclude = {"tourDayServices", "serviceDetails"})
public class Service extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "nett_price")
    private double nettPrice;

    @Column(name = "selling_price")
    private double sellingPrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ServiceCategory serviceCategory;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    @ToString.Exclude
    private ServiceProvider serviceProvider;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "service")
    @ToString.Exclude
    private List<TourDayService> tourDayServices;
}
//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "service")
//    private Set<ServiceDetail> serviceDetails;

