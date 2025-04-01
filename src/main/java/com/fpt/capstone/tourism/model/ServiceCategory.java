package com.fpt.capstone.tourism.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "service_category")
public class ServiceCategory extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @ManyToMany(mappedBy = "serviceCategories")
    @ToString.Exclude
    private List<ServiceProvider> serviceProviders;


    @OneToMany(mappedBy = "serviceCategory")
    @ToString.Exclude
    private List<TourDayServiceCategory> tourDayServiceCategories;
}
