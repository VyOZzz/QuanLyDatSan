package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // Thời gian hết hạn thanh toán
    @Column(nullable = false)
    private LocalDateTime expireTime;

    // Thông tin chứng minh chuyển khoản
    @Column
    private Boolean paymentProofUploaded = false;

    @Column
    private String paymentProofUrl;

    @Column
    private LocalDateTime paymentProofUploadedAt;

    // Lý do từ chối (nếu có)
    @Column(length = 500)
    private String rejectionReason;
}
