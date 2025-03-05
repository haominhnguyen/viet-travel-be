package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
@Data
@Builder
public class BookedPersonDTO {
    private String fullName;
    private String phone;
    private String email;
    private String address;
}

