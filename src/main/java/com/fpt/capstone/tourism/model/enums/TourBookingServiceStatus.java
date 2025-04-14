package com.fpt.capstone.tourism.model.enums;

public enum TourBookingServiceStatus {
    //    Pending, Approved, Rejected, Wait Confirmed
    PENDING,        // Đang chờ xử lý (đã gửi yêu cầu, chờ phản hồi từ nhà cung cấp)
    APPROVED,       // Đã được phê duyệt (nhà cung cấp đã xác nhận)
    REJECTED,       // Bị từ chối (nhà cung cấp từ chối dịch vụ)
    NOT_ORDERED,    // Chưa đặt dịch vụ (chưa chuyển điều hành, chưa xác thực)

    PAID, //Dieu hanh thanh toan dich vu

    SUCCESS, //Đặt dịch vụ thành công
    NOT_AVAILABLE,   // Dịch vụ không khả dụng
    AVAILABLE,    //Dịch vụ khả dụng
    CHECKING, // Yêu cầu dịch vụ khả dụng hay không

    CHANGE_REQUEST,   //Yêu cầu điều hành thay đổi dịch vụ (thay đổi số lượng)

    CANCELLED,         // BỊ hủy


    ADD_REQUEST,    // Yêu cầu điều hành thêm dịch vụ
    CANCEL_REQUEST, // Yêu cầu điều hành hủy dịch vụ khi dịch vụ chưa được order
    REJECTED_BY_OPERATOR, // Nhà điều hành từ chối yêu cầu thay đổi

    CHANGED,           //1 phần được đặt, 1 phần thì chưa
    PENDING_CANCEL_REQUEST,   //Yêu cầu điều hành hủy dịch vụ khi dịch vụ đang pending
    APPROVE_CANCEL_REQUEST,  //Yêu cầu điều hành hủy dịch vụ khi dịch vụ đã đặt thành công
}
