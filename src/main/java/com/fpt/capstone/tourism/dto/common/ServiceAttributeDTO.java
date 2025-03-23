package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAttributeDTO {
    private String name;
    private String value;
}
