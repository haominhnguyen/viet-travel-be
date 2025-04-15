package com.fpt.capstone.tourism.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourPaxCreateRequestDTO {
    private int minPax;
    private int maxPax;
    private Double fixedCost;
    private Double extraHotelCost;
    private Double nettPricePerPax;
    private Double sellingPrice;
    private Date validFrom;
    private Date validTo;
    private List<ServicePricingRequestDTO> servicePricings;
}