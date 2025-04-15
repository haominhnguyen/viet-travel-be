package com.fpt.capstone.tourism.dto.common;
import com.fpt.capstone.tourism.model.Service;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RoomSimpleDTO {
    private Long id;
    private Integer capacity; // Số khách tối đa trong phòng
    private Integer availableQuantity; // Số lượng phòng còn lại
    private String facilities; // Danh sách các dịch vụ trong phòng (bồn tắm, tủ lạnh, wifi)
}
