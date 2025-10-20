package com.codewithvy.quanlydatsan.entity;

import com.codewithvy.quanlydatsan.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity biểu diễn thông báo trong hệ thống
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Người gửi thông báo (optional, có thể là hệ thống)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // Booking liên quan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Loại thông báo
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // Tiêu đề thông báo
    @Column(nullable = false)
    private String title;

    // Nội dung thông báo
    @Column(nullable = false, length = 1000)
    private String message;

    // Đã đọc chưa
    @Column(nullable = false)
    private Boolean isRead = false;
}

