package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity biểu diễn một lượt đặt sân (booking) với khung giờ và ngày cụ thể.
 * Gắn với một Court và một Payment tương ứng.
 */
@Entity
@Table(name = "booked_court")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedCourt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookedCourtId; // id booking

    @Column(nullable = false)
    private LocalTime timeStart; // giờ bắt đầu
    @Column(nullable = false)
    private LocalTime timeEnd;   // giờ kết thúc
    @Column(nullable = false)
    private LocalDate dayBooked; // ngày đặt

    @OneToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment; // thanh toán gắn với booking

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court; // sân được đặt
}
