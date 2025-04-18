package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ServiceProviderSimpleDTO {
    private Long id;
    private String name;
    private String abbreviation;
    private String imageUrl;
    private String address;
}
