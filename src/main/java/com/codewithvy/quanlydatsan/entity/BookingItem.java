package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity biểu diễn một slot sân được đặt trong một booking.
 * Đây là chi tiết của đơn đặt sân (booking detail).
 */
@Entity
@Table(name = "booking_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking; // Đơn đặt sân chứa item này

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court; // Sân được đặt

    @Column(nullable = false)
    private LocalDateTime startTime; // Thời gian bắt đầu

    @Column(nullable = false)
    private LocalDateTime endTime; // Thời gian kết thúc

    @Column(nullable = false)
    private Double price; // Giá của slot này
}

