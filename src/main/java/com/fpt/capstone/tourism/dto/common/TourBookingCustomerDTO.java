package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.enums.Gender;
import com.fpt.capstone.tourism.model.enums.AgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
