package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.enums.Gender;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.enums.AgeType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TourBookingCustomerDTO {
    private Long id;
    private String fullName;
    private String address;
    private String email;
    private Date dateOfBirth;
    private String phoneNumber;
    private String pickUpLocation;
    private String note;
    private Gender gender;
    private AgeType ageType;
    private Boolean singleRoom;
    private Boolean deleted;
    private Boolean bookedPerson;
}
