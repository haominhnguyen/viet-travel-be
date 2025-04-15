package com.fpt.capstone.tourism.model;


import com.fpt.capstone.tourism.model.enums.Gender;
import com.fpt.capstone.tourism.model.enums.AgeType;
import jakarta.persistence.*;
import lombok.*;

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
    @ToString.Exclude
    private TourBooking tourBooking;

    @Column(name = "customer_name")
    private String fullName;

    private String address;

    private String email;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "phone_number")
    private String phoneNumber;


    @Column(name = "pick_up_location")
    private String pickUpLocation;

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

    @Column(name = "booked_person", updatable = false)
    private Boolean bookedPerson;
}
