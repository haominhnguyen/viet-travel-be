package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDTO {
    private String username;
    private String token;
    private String expirationTime;
}
