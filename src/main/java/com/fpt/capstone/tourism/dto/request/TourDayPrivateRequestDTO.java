package com.fpt.capstone.tourism.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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

    @NotNull(message = "Chưa nhập thông tin ngày")
    private String content;

    @NotNull(message = "Chưa nhập thông tin số ngày")
    private int dayNumber;

    private Boolean deleted;

    private Long locationId;

    @NotNull(message = "Phải chọn ít nhất một dịch vụ")
    @Size(min = 1, message = "Phải chọn ít nhất một dịch vụ")
    private List<Long> serviceCategoryIds;
}
