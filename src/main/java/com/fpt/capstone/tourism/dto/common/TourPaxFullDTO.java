package com.fpt.capstone.tourism.dto.common;

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

public class TourPaxFullDTO {
    private Long id;
    private Long tourId;
    private int minPax;
    private int maxPax;
    private String paxRange;
    private Double fixedCost;
    private Double extraHotelCost;
    private Double nettPricePerPax;
    private Double sellingPrice;
    private Date validFrom;
    private Date validTo;
    private boolean isValid;
    private boolean isDeleted;
    private List<TourDayServicePricingDTO> serviceAssociations;
    private Integer serviceAssociationCount;
}
