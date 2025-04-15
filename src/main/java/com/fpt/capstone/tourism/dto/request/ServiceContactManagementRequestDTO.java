package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.enums.Gender;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceContactManagementRequestDTO {
    private String position;
    private String fullName;
    private String phoneNumber;
    private String email;
    private Gender gender;
}

