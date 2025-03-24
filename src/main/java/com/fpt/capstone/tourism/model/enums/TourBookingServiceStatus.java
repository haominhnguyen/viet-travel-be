package com.fpt.capstone.tourism.model.enums;

public enum TourBookingServiceStatus {
    //    Pending, Approved, Rejected, Wait Confirmed
    PENDING,        // Đang chờ xử lý (đã gửi yêu cầu, chờ phản hồi từ nhà cung cấp)
    APPROVED,       // Đã được phê duyệt (nhà cung cấp đã xác nhận)
    REJECTED,       // Bị từ chối (nhà cung cấp từ chối dịch vụ)
    NOT_ORDERED,    // Chưa đặt dịch vụ (dù có trong danh sách nhưng chưa gửi yêu cầu)
    ADD_REQUEST,    // Yêu cầu điều hành thêm dịch vụ
    CANCEL_REQUEST, // Yêu cầu điều hành hủy dịch vụ
    REJECTED_BY_OPERATOR, // Nhà điều hành từ chối yêu cầu thay đổi
    CANCELLED,         // BỊ hủy
    CHANGED,           //1 phần được đặt, 1 phần thì chưa
}
