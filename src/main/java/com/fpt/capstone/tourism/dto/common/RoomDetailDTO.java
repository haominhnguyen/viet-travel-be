package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailDTO {
    private Long id;
    private Integer capacity;
    private Integer availableQuantity;
    private String facilities;
}
