package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity biểu diễn dịch vụ đi kèm tại một Venue (ví dụ: thuê giày, nước uống...).
 */
@Entity
@Table(name = "service")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id dịch vụ

    @Column(nullable = false)
    private String nameService; // tên dịch vụ

    @Column(nullable = false)
    private Long price; // giá dịch vụ (đơn vị tuỳ ý)

    // Thêm liên kết trực tiếp với Venue
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venues venue; // Dịch vụ thuộc về venue nào

    // Giữ lại VenuesDetail để tương thích ngược (có thể xóa sau)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venues_detail_id")
    private VenuesDetail venuesDetail;
}
