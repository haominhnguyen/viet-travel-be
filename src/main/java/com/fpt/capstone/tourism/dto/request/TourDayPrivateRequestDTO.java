package com.fpt.capstone.tourism.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TourDayPrivateRequestDTO {
    private Long id;
    @NotNull(message = "Tên ngày không được rỗng")
    @NotBlank(message = "Tên ngày không được rỗng")
    private String title;

    @NotNull(message = "Chưa nhập thông tin bữa ăn")
    @NotBlank(message = "Thông tin bữa ăn Không được rỗng")
    private String meals;

    @NotNull(message = "Chưa nhập thông tin số ngày")
    private int dayNumber;
}
