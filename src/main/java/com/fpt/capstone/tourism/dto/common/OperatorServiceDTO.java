package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OperatorServiceDTO {
    private Long bookingServiceId;
    private Long bookingId;
    private Long serviceId;
    private String providerName;
    private String providerEmail;
    private String location;
    private String bookingCode;
    private String serviceName;
    private String serviceCategory;
    private LocalDateTime usingDate;
    private Integer requestQuantity;
    private Integer currentQuantity;
    private String bookingStatus;
    private Double paidForBooking; //Số tiền đã trả cho nhà cung cấp
    private Double amountToPayForBooking; //Số tiền phải trả cho nhà cung cấp
    private String paymentStatus;
    private Long tourDayId;
}
