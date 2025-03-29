package com.fpt.capstone.tourism.dto.request;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourPaxUpdateRequestDTO {
    private Integer minPax;
    private Integer maxPax;
    private Double fixedCost;
    private Double extraHotelCost;
    private Double nettPricePerPax;
    private Double sellingPrice;
    private Date validFrom;
    private Date validTo;
    private List<ServicePricingRequestDTO> servicePricings;
}
