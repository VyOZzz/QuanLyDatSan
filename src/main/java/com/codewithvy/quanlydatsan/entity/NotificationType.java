package com.codewithvy.quanlydatsan.entity;

/**
 * Enum định nghĩa các loại thông báo trong hệ thống
 */
public enum NotificationType {
    PAYMENT_UPLOADED,         // Người dùng đã upload chứng minh chuyển khoản
    BOOKING_CONFIRMED,        // Chủ sân đã xác nhận booking
    BOOKING_REJECTED,         // Chủ sân đã từ chối booking
    BOOKING_EXPIRED,          // Booking hết hạn thanh toán
    REVIEW_RECEIVED           // Chủ sân nhận được đánh giá mới
}
