package com.codewithvy.quanlydatsan.entity;

import com.codewithvy.quanlydatsan.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity biểu diễn liên lạc/contact từ người dùng
 */
@Entity
@Table(name = "lien_lac")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LienLac extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người gửi liên lạc (có thể là user đã đăng nhập hoặc khách)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Tên người liên hệ (bắt buộc)
    @Column(nullable = false, length = 100)
    private String name;

    // Email người liên hệ (bắt buộc)
    @Column(nullable = false, length = 100)
    private String email;

    // Số điện thoại người liên hệ
    @Column(length = 20)
    private String phone;

    // Chủ đề liên hệ
    @Column(nullable = false, length = 200)
    private String subject;

    // Nội dung liên hệ
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // Trạng thái xử lý
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LienLacStatus status = LienLacStatus.PENDING;

    // Venue liên quan (optional - nếu liên hệ về một sân cụ thể)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venues venue;

    // Người xử lý (admin/owner)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id")
    private User handler;

    // Ghi chú phản hồi từ người xử lý
    @Column(columnDefinition = "TEXT")
    private String response;
}
