package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.dto.NotificationDTO;
import com.codewithvy.quanlydatsan.entity.Booking;
import com.codewithvy.quanlydatsan.entity.NotificationType;
import com.codewithvy.quanlydatsan.entity.User;

import java.util.List;

public interface NotificationService {

    /**
     * Tạo thông báo mới
     */
    void createNotification(User recipient, User sender, Booking booking,
                          NotificationType type, String title, String message);

    /**
     * Lấy tất cả thông báo của user hiện tại
     */
    List<NotificationDTO> getMyNotifications();

    /**
     * Đếm số thông báo chưa đọc
     */
    Long getUnreadCount();

    /**
     * Đánh dấu một thông báo là đã đọc
     */
    void markAsRead(Long notificationId);

    /**
     * Đánh dấu tất cả thông báo là đã đọc
     */
    void markAllAsRead();

    /**
     * Xóa một thông báo
     */
    void deleteNotification(Long notificationId);
}

