package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChangeServiceDTO {
    private Long tourBookingServiceId;
    private String tourName;
    private String tourType;
    private Integer dayNumber;
    private String bookingCode;
    private String reason;
    private String proposer; //nguoi de xuat
    private String status;
    private LocalDateTime updatedAt;
}
