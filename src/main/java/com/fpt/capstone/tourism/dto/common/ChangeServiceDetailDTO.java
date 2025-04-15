package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChangeServiceDetailDTO {
    private Long tourBookingServiceId;
    private String tourName;
    private String tourType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer dayNumber;
    private String bookingCode;
    private String status;
    private String reason;
    private String proposer; //nguoi de xuat
    private LocalDateTime updatedAt;
    private String serviceName;
    private Double nettPrice;
    private Integer currentQuantity;
    private Integer requestQuantity;
    private Double totalPrice;
}
