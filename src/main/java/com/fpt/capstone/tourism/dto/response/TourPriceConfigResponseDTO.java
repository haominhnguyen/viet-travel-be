package com.fpt.capstone.tourism.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourPriceConfigResponseDTO {
    private Long id;
    private Long tourId;
    private String tourName;
    private Integer minPax;
    private Integer maxPax;
    private String paxRange;
    private Double nettPricePerPax;
    private Double sellingPrice;
    private Double fixedCost;
    private Double extraHotelCost;
    private Date validFrom;
    private Date validTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
