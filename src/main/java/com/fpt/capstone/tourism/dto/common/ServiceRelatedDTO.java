package com.fpt.capstone.tourism.dto.common;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRelatedDTO {
    private List<ServiceDetailDTO> serviceDetails;
    private List<TourDayServiceDTO> tourDayServices;
    private List<TourDayDTO> tourDays;
}

