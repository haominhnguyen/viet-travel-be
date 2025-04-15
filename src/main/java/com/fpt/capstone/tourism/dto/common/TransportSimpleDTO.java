package com.fpt.capstone.tourism.dto.common;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TransportSimpleDTO {
    private Long id;
    private Integer seatCapacity; // Số ghế trên phương tiện

}
