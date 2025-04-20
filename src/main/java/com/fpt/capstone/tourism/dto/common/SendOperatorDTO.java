package com.fpt.capstone.tourism.dto.common;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendOperatorDTO {
    private Long tourId;
    private Long tourScheduleId;
}
