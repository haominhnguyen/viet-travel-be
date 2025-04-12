package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourGuideRequestDTO {
    private String fullName;
    private String username;
    private String password;
    private String email;
    private Gender gender;
    private String phone;
    private String address;
    private String avatarImage;
}
