package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PublicServiceDTO {
    private Long serviceId;
    private String name;
    private double sellingPrice;
    private String imageUrl;
    private Long roomId;
    private Integer capacity; // Số khách tối đa trong phòng
    private Integer availableQuantity; // Số lượng phòng còn lại
    private String facilities; // Danh sách các dịch vụ trong phòng (bồn tắm, tủ lạnh, wifi)
}
