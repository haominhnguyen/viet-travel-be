package com.fpt.capstone.tourism.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateDTO {
    private Integer capacity;
    private Integer availableQuantity;
    private String facilities;
}
