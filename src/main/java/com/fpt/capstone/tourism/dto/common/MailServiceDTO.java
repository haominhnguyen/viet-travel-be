package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailServiceDTO {
    private Long bookingServiceId;
    private Long providerId;
    private String providerName;
    private String providerEmail;
    private String emailSubject;
    private String emailContent;
}
