package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity biểu diễn dịch vụ đi kèm tại một VenuesDetail (ví dụ: thuê giày, nước uống...).
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venues_detail_id", nullable = false)
    private VenuesDetail venuesDetail; // VenuesDetail chứa dịch vụ này
}
