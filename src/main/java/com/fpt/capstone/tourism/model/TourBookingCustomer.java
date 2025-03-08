package com.fpt.capstone.tourism.model;


import com.fpt.capstone.tourism.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tour_booking_customer")
public class TourBookingCustomer extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tour_booking_id")
    private TourBooking tourBooking;

    @Column(name = "customer_name")
    private String customerName;
    private String email;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String note;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "customer_type")
    @Enumerated(EnumType.STRING)
    private AgeType ageType;

    @Column(name = "single_room")
    private Boolean singleRoom;

    @Column(name = "is_deleted")
    private Boolean deleted;
}
