package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private List<RevenueChartDTO> monthlyRevenue;       // Doanh thu từng tháng (Line Chart)
    private List<NewUsersChartDTO> monthlyNewUsers;      // Tài khoản mới từng tuần (Bar Chart)
    private List<TourTypeRatioDTO> tourTypeRatios;      // Tỉ lệ tour SIC vs Private (Line Chart)
    private List<RecentBookingDTO> recentBookings;      // Danh sách booking gần đây
    private List<TopRevenueTourDTO> topRevenueTours;    // Top tour có doanh thu cao nhất
    private Integer cancelBookingNumber;                //Số lượng booking bị hủy
    private Integer onlineBookingNumber;                //Số lượng booking qua kênh online
    private Integer offlineBookingNumber;               //Số lượng booking qua kênh offline
    private Integer returnCustomerNumber;               //Số lượng khách quay lại
}

