package com.fpt.capstone.tourism.dto.response;


import com.fpt.capstone.tourism.enums.Gender;

import com.fpt.capstone.tourism.enums.RoleName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Gender gender;
    private String phone;
    private String address;
    private String avatarImg;
    private RoleName role;

}
