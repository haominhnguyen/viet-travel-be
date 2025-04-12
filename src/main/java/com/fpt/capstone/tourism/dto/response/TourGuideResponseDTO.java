package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourGuideResponseDTO {
    private Long id;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private Gender gender;
    private String phone;
    private String address;
    private String avatarImage;
    private boolean deleted;
    private List<String> roleNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public void setRoles(List<String> roles) {
        this.roleNames = roles;
    }
}
