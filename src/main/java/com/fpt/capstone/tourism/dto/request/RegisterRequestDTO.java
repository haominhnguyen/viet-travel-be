package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    private String username;
    private String fullName;
    private String email;
    private String password;
    private String rePassword;
    private Gender gender;
    private String phone;
    private String address;
}