package com.fpt.capstone.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String avatarImage;
}
