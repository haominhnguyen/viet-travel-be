package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookedCustomerDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
}
