package com.codewithvy.quanlydatsan.entity;

public enum BookingStatus {
    PENDING_PAYMENT,      // Chờ chuyển khoản (mới tạo, hiển thị thông tin TK)
    PAYMENT_UPLOADED,     // Đã upload chứng minh chuyển khoản
    CONFIRMED,            // Chủ sân xác nhận
    REJECTED,             // Chủ sân từ chối
    CANCELLED,            // Hủy bởi người dùng
    EXPIRED,              // Hết hạn thanh toán
    COMPLETED             // Hoàn thành
}
