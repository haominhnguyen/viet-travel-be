package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequestDTO {
    private Boolean isDeleted;
    private Long providerId;
}
