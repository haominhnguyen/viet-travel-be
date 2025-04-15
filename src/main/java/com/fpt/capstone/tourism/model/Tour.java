package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tour")
public class Tour extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(columnDefinition = "text")
    private String highlights;

    @Column(name = "number_day")
    private int numberDays;
    @Column(name = "number_night")
    private int numberNights;

    private String note;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinTable(name = "tour_location",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id"))
    private List<Location> locations;

    @ManyToMany(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinTable(
            name = "tour_tag",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @Column(name = "tour_type")
    @Enumerated(EnumType.STRING)
    private TourType tourType;

    @Column(name = "tour_status")
    @Enumerated(EnumType.STRING)
    private TourStatus tourStatus;

    @OneToMany(mappedBy = "tour")
    @ToString.Exclude
    private Set<TourPax> tourPax;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "depart_location_id")
    private Location departLocation;

    @Column(name = "mark_up_percent")
    private double markUpPercent;

    @Column(columnDefinition = "text")
    private String privacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TourSchedule> tourSchedules;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourImage> tourImages;

    @ToString.Exclude
    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY)
    private List<TourDay> tourDays;
}
