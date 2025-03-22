package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.TourDayService;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OperatorServiceDetailDTO {
    private Long id;
    private String name;
    private double nettPrice;
    private double sellingPrice;
    private String imageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String serviceCategory;
    private String serviceProvider;
    private RoomSimpleDTO room;
    private MealSimpleDTO meal;
    private TransportSimpleDTO transport;
}
