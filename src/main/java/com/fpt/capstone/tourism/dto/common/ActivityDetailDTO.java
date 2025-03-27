package com.fpt.capstone.tourism.dto.common;
import jakarta.persistence.Column;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDetailDTO {
    private Long id;
    private String name;
}
