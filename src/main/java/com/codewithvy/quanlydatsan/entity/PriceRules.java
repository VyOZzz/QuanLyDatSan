package com.codewithvy.quanlydatsan.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

/**
 * Quy tắc tính giá theo khung giờ cho Venues.
 * Ví dụ: 06:00-10:00 giá X (giờ sáng), 10:00-17:00 giá Y (giờ trưa).
 * Chủ sân có thể tự cài đặt nhiều khung giờ với giá khác nhau.
 */
@Entity
@Table(name = "price_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Tên khung giờ (VD: "Giờ cao điểm buổi sáng", "Giờ thường")

    @Column(nullable = false)
    private LocalTime startTime; // giờ bắt đầu áp dụng

    @Column(nullable = false)
    private LocalTime endTime;   // giờ kết thúc áp dụng

    @Column(nullable = false)
    private Double pricePerHour;   // đơn giá theo giờ trong khung này (VND)

    @Column(nullable = false)
    private boolean active = true; // Trạng thái kích hoạt (mặc định: đang hoạt động)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venues_id", nullable = false)
    @JsonBackReference("venues-pricerules")
    private Venues venues; // Venues mà rule này áp dụng
}
