package com.codewithvy.quanlydatsan.dto;

import com.codewithvy.quanlydatsan.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO cho Notification response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long bookingId;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private Instant createdAt;  // Đổi từ LocalDateTime sang Instant
    private String senderName; // Tên người gửi (nếu có)
}
