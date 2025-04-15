package com.fpt.capstone.tourism.dto.request;


import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePaymentMethodDTO {
    private Long bookingId;
    private PaymentMethod paymentMethod;
}
