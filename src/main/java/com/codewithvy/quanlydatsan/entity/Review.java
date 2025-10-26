package com.codewithvy.quanlydatsan.entity;

import com.codewithvy.quanlydatsan.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity lưu đánh giá của người dùng về một Venues sau khi hoàn thành booking.
 */
@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người dùng đã đánh giá (USER)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Venues được đánh giá
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venues venues;

    // Booking liên quan - đảm bảo chỉ review khi đã hoàn thành booking
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    // Đánh giá từ 1-5 sao
    @Column(nullable = false)
    private Integer rating;

    // Nhận xét văn bản
    @Column(columnDefinition = "TEXT")
    private String comment;

    // Ràng buộc: rating phải từ 1-5
    @PrePersist
    @PreUpdate
    private void validate() {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}

