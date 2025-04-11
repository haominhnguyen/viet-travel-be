package com.fpt.capstone.tourism.dto.request;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendEmailPriceRequestDTO {
    private String email;
    private String subject;
    private String content;
}
