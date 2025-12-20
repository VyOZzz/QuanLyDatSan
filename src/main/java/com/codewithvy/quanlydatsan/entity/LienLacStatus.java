package com.codewithvy.quanlydatsan.entity;

/**
 * Enum biểu diễn trạng thái của liên lạc/contact
 */
public enum LienLacStatus {
    PENDING,    // Chờ xử lý
    PROCESSING, // Đang xử lý
    RESOLVED,   // Đã giải quyết
    CLOSED      // Đã đóng
}
