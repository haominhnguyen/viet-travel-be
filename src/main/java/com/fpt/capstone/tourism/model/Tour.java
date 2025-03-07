package com.fpt.capstone.tourism.model;

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

    @ManyToMany
    @JoinTable(name = "tour_location",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id"))
    private List<Location> locations;


    @ManyToMany
    @JoinTable(
            name = "tour_tag",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    private boolean opened;

    @OneToMany(mappedBy = "tour")
    private Set<TourPax> tourPax;

    @ManyToOne
    @JoinColumn(name = "depart_location_id")
    private Location departLocation;

    @Column(name = "mark_up_percent")
    private double markUpPercent;

    @Column(columnDefinition = "text")
    private String privacy;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourSchedule> tourSchedules;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourImage> tourImages;

    @OneToMany(mappedBy = "tour")
    private List<TourDay> tourDays;
}
