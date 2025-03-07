package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
}
