package com.fpt.capstone.tourism.dto.request;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourPriceConfigRequestDTO {
    private Long tourId;
    private Double sellingPrice;
    private Double fixedCost;
    private Double extraHotelCost;
    private Double nettPricePerPax;
    private Date validFrom;
    private Date validTo;
    private Long id;
}
