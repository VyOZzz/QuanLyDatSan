package com.codewithvy.quanlydatsan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private boolean isBooked; // trạng thái đã được đặt hay chưa (đơn giản hoá)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venues_id", nullable = false)
    @JsonIgnore // tránh vòng lặp Venues -> Courts -> Venues khi serialize JSON
    private Venues venues; // venues mà sân này trực thuộc

    // Xoá relation tới BookedCourt vì entity này đã bị loại khỏi dự án.
    // @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    // private java.util.List<BookedCourt> bookedCourts;
}
