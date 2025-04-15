package com.fpt.capstone.tourism.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceQuantityUpdateDTO {
    @NotNull
    private Long tourBookingServiceId;

    @NotNull
    @Min(1) // Đảm bảo số lượng mới phải >= 1
    private Integer newQuantity;
}
