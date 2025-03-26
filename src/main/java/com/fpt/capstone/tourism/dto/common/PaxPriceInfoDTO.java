package com.fpt.capstone.tourism.dto.common;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaxPriceInfoDTO {
    private Long paxId;
    private int minPax;
    private int maxPax;
    private String paxRange;
    //private Double price;
}
