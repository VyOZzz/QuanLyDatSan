package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Entity biểu diễn địa điểm (Venues) quản lý nhiều sân (Court), gắn một địa chỉ.
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


    @Column(columnDefinition = "TEXT")
    private String description; // mô tả về venues

    // Danh sách ảnh
    @ElementCollection
    @CollectionTable(name = "venues_images", joinColumns = @JoinColumn(name = "venue_id"))
    @Column(name = "image")
    private List<String> images = new ArrayList<>();

    @Column
    private String phoneNumber; // số điện thoại liên hệ

    @Column
    private String email; // email liên hệ

    // Giá cố định theo giờ cho tất cả sân trong venues (VND)
    @Column(nullable = false)
    private Double pricePerHour = 100000.0; // Mặc định 100,000 VND/giờ

    // Thời gian hoạt động
    @Column(name = "opening_time")
    private LocalTime openingTime; // Giờ mở cửa (VD: 06:00:00)

    @Column(name = "closing_time")
    private LocalTime closingTime; // Giờ đóng cửa (VD: 23:00:00)

    // Chủ sân - người sở hữu venues (bắt buộc)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "venues", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // parent reference to allow serializing courts without cycle
    private List<Court> courts; // danh sách sân trực thuộc venues

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address; // địa chỉ nơi venues tọa lạc

    // XÓA averageRating và totalReviews - sẽ tính toán động từ bảng review
    // Tuân thủ chuẩn 3NF: không lưu dữ liệu dẫn xuất
}
