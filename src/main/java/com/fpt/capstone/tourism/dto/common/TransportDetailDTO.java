package com.fpt.capstone.tourism.dto.common;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportDetailDTO {
    private Long id;
    private Integer seatCapacity;
}
