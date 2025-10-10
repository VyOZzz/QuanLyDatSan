package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entity lưu địa chỉ hành chính (tỉnh/thành, quận/huyện, địa chỉ chi tiết).
 * Một địa chỉ có thể gắn với nhiều Venues.
 */
@Entity
@Table(name = "address")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id địa chỉ

    @Column(nullable = false)
    private String provinceOrCity; // Tỉnh/Thành phố
    @Column(nullable = false)
    private String district; // Quận/Huyện
    @Column(nullable = false)
    private String detailAddress; // Địa chỉ chi tiết

    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // tránh vòng lặp Address -> Venues -> Address khi serialize JSON
    private List<Venues> venues; // danh sách venues thuộc địa chỉ này
}
