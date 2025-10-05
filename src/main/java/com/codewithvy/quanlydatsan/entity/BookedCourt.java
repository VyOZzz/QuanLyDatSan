package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "booked_court")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedCourt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookedCourtId;

    @Column(nullable = false)
    private LocalTime timeStart;
    @Column(nullable = false)
    private LocalTime timeEnd;
    @Column(nullable = false)
    private LocalDate dayBooked;

    @OneToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;
}
