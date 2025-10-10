package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Entity biểu diễn thanh toán cho một lượt đặt sân (giản lược).
 */
@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId; // id thanh toán

    @Column(nullable = false)
    private Boolean status; // trạng thái thanh toán (đã/đang/chưa) – tuỳ định nghĩa
    @Column(nullable = false)
    private Long totalPrice; // tổng tiền
    @Column(nullable = false)
    private LocalTime expireTime; // thời điểm hết hạn giữ chỗ/thanh toán (giả định)
}
