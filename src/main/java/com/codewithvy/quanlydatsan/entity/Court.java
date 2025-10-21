package com.codewithvy.quanlydatsan.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity biểu diễn một sân (Court) thuộc về một địa điểm (Venues).
 */
@Entity
@Table(name = "court")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id sân

    private String description; // mô tả sân

    private boolean isBooked; // trạng thái đã được đặt hay chưa (đơn giản hoá)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venues_id", nullable = false)
    @JsonBackReference // back reference to avoid cyclic serialization (child -> parent)
    private Venues venues; // venues mà sân này trực thuộc
}
