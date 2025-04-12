package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.enums.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaleCreateUserRequestDTO {
    private String fullName;
    private String username;
    private String email;
    private Gender gender;
    private String password;
    private String phone;
    private String address;
    private boolean emailConfirmed;
}
