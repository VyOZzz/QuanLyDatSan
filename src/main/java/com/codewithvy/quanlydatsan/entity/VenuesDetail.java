package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Entity lưu thông tin chi tiết bổ sung cho Venues (tiêu đề, mô tả, danh sách ảnh).
 */
@Entity
@Table(name = "venues_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenuesDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id chi tiết venues

    @Column(nullable = false)
    private String title; // tiêu đề mô tả
    @Column(nullable = false)
    private String description; // mô tả chi tiết

    @ElementCollection
    @CollectionTable(name = "venues_detail_images", joinColumns = @JoinColumn(name = "venues_detail_id"))
    @Column(name = "image")
    private List<String> images; // danh sách URL hoặc path ảnh

    @OneToOne(optional = false)
    @JoinColumn(name = "venues_id", unique = true, nullable = false)
    private Venues venues; // liên kết 1-1 tới Venues
}
