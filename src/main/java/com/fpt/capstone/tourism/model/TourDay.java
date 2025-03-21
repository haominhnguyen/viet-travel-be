package com.fpt.capstone.tourism.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table (name = "tour_day")
public class TourDay extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_number")
    private Integer dayNumber;

    @Column(name = "day_title")
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "meal_plan")
    private String mealPlan;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "tourDay")
    private Set<TourDayService> tourDayServices;
}
