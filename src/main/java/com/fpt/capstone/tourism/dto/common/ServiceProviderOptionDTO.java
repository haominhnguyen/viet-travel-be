package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderOptionDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer star;
    private String phone;
    private String email;
    private String address;
}
