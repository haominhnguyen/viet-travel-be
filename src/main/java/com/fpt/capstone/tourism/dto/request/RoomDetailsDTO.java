package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailsDTO {
    private Integer capacity;
    private Integer availableQuantity;
    private String facilities;
}
