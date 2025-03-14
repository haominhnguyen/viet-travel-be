package com.fpt.capstone.tourism.model;

import com.fpt.capstone.tourism.model.enums.MealType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "meal")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Meal extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType type;  // Loại bữa ăn (Sáng, Trưa, Tối)

    @OneToOne
    @JoinColumn(name = "service_id", nullable = false, unique = true)
    private Service service;  // Liên kết với Service nếu Meal là một loại dịch vụ

    @Column(name = "is_deleted")
    private Boolean deleted;

    @Column(columnDefinition = "text")
    private String mealDetail;
}

