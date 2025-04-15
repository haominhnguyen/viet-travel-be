package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.enums.TourBookingCategory;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OperatorTourBookingDTO {
    private Long bookingId;
    private String bookingCode;
    private String bookedBy;                      //Tên khách hàng đứng ra book tour
    private Integer adultCount;                   //Số lượng kháck người lớn trong tour
    private Integer childCount;                   //Số lượng khách trẻ em trong tour
    private Integer customerCount;                //Tổng khách trong tour
    private TourBookingCategory bookingCategory;
    private Double receiptAmount;                 //Số tiền đã thu
    private Double remainingAmount;               //Số tiền còn phải thu
    private Double collectionAmount;              //Số tiền HDV đã thu hộ
    private Double totalAmount;                   //Tổng tiền phải thu
    private LocalDateTime bookedAt;
    private TourBookingStatus bookingStatus;
    private String saleName;
}
