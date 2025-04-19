package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TourCustomerDTO {
    private String fullName;
    private Date dateOfBirth;
    private Gender gender;
    private Boolean singleRoom;
}
