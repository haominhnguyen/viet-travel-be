package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDetailRequestDTO {
    private String title;
    private String content;
    private Boolean deleted;
}
