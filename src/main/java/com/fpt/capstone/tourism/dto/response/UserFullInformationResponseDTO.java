package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.enums.Gender;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFullInformationResponseDTO {
    private Long id;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private Gender gender;
    private String phone;
    private String address;
    private String avatarImage;
    private List<String> roleNames;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setRoles(List<String> roles) {
        this.roleNames = roles;
    }
}
