package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourBookingAccountantShortResponseDTO {
    private Long id;
    private String bookingCode;
    private TourBookingStatus status;
}
