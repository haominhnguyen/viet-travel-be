package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity")
public class Activity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name="price_per_person")
    private double pricePerPerson;

    @Column(name="is_deleted")
    private Boolean deleted;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "geo_position_id")
    private GeoPosition geoPosition;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "activity")
    private Set<TourDayActivity> tourDayActivities;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ActivityCategory activityCategory;
}
