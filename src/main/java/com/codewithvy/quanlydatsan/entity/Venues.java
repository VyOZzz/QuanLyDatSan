package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Entity biểu diễn địa điểm (Venues) quản lý nhiều sân (Court), gắn một địa chỉ và có chi tiết.
 */
@Entity
@Table(name = "venues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venues {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id venues

    @Column(nullable = false)
    private int numberOfCourt; // số sân đăng ký/quản lý

    @Column(nullable = false)
    private String name; // tên địa điểm

    @OneToMany(mappedBy = "venues", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Court> courts; // danh sách sân trực thuộc venues

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address; // địa chỉ nơi venues tọa lạc

    @OneToOne(mappedBy = "venues", cascade = CascadeType.ALL, orphanRemoval = true)
    private VenuesDetail venuesDetail; // thông tin chi tiết (mô tả, hình ảnh)
}
