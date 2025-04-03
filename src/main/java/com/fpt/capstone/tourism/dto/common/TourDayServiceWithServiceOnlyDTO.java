package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.TourDay;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourDayServiceWithServiceOnlyDTO {
    private Long id;
    private ServiceForTourPrivateDTO service;
}
