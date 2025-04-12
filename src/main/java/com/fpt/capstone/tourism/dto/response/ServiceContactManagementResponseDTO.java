package com.fpt.capstone.tourism.dto.response;


import com.fpt.capstone.tourism.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceContactManagementResponseDTO {
    private Long id;
    private String position;
    private String fullName;
    private String phoneNumber;
    private String email;
    private Gender gender;
    private Long serviceProviderId;
    private String serviceProviderName;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
