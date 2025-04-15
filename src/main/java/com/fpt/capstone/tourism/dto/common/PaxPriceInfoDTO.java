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
    private Double price;  // nett price per pax from TourPax
    private Double serviceNettPrice; // nett price from Service
    private Double sellingPrice;     // selling price from TourPax
    private Double fixedCost;
    private Double extraHotelCost;
}
