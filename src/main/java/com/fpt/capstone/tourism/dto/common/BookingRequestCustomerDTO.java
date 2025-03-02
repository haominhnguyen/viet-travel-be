package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class BookingRequestCustomerDTO {
    private String fullName;
    private Gender gender;
    private Date dateOfBirth;
    private boolean singleRoom;
}
