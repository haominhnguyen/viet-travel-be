package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingRequestDTO {
    private Long userId;
    private Long tourId;
    private Long scheduleId;
    private String fullName;
    private String note;
    private String phone;
    private String address;
    private PaymentMethod paymentMethod;
    private String email;
    private List<BookingRequestCustomerDTO> adults;
    private List<BookingRequestCustomerDTO> children;
    private Double total;
    private Double sellingPrice;
    private Double extraHotelCost;

}
