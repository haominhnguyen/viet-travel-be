package com.fpt.capstone.tourism.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "service_provider")
public class ServiceProvider extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    private String name;

    private String abbreviation;

    private String website;

    private String email;

    private int star;

    private String phone;

    private String address;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "geo_position_id")
    @ToString.Exclude
    private GeoPosition geoPosition;

    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @ManyToMany
    @ToString.Exclude
    @JoinTable(
            name = "provide_service",
            joinColumns = @JoinColumn(name = "provider_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<ServiceCategory> serviceCategories;
}
