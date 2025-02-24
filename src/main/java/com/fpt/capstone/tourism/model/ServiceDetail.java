package com.fpt.capstone.tourism.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "service_details")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDetail extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;  // Reference to the associated Service

    @Column(name = "title", nullable = false)
    private String title;  // The label or type of detail (e.g., "Bed Type", "Cart Type")

    @Column(name = "content", nullable = false)
    private String content;  // The actual detail information (e.g., "King-size", "Luxury Bus")

}
