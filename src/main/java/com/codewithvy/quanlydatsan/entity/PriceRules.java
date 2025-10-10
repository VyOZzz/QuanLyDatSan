package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

/**
 * Quy tắc tính giá theo khung giờ cho một Court.
 * Ví dụ: 06:00-10:00 giá X, 10:00-17:00 giá Y.
 */
@Entity
@Table(name = "price_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id rule

    @Column(nullable = false)
    private LocalTime startTime; // giờ bắt đầu áp dụng
    @Column(nullable = false)
    private LocalTime endTime;   // giờ kết thúc áp dụng
    @Column(nullable = false)
    private Long pricePerHour;   // đơn giá theo giờ trong khung này

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court; // sân mà rule này áp dụng
}
