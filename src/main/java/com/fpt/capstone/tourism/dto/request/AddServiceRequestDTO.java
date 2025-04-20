package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.model.Service;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddServiceRequestDTO {
    private Long bookingId;
    private Long serviceId;
    @Min(value = 1, message = "Số lượng dịch vụ phải lớn hơn 0")
    private Integer addQuantity;
    private LocalDateTime requestDate;
    private String reason;
    private Long tourDayId;
}
