package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TourCustomerDTO {
    private String fullName;
    private Date dateOfBirth;
    private Gender gender;
    private Boolean singleRoom;
}
