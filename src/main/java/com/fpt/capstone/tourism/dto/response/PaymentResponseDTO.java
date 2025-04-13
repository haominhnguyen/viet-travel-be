package com.fpt.capstone.tourism.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDTO {
    private String paymentStatus;
    private String bookingCode;
}
