package com.fpt.capstone.tourism.dto.common;


import com.fpt.capstone.tourism.model.Tour;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TourPaxDTO {
    private Long id;
    private Double fixedCost;
    private int minPax;
    private int maxPax;
    private Double extraHotelCost;
    private Double nettPricePerPax;
    private Double sellingPrice;
    private Date validFrom;
    private Date validTo;
}
