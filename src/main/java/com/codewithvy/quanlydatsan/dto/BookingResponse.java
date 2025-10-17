package com.codewithvy.quanlydatsan.dto;

import com.codewithvy.quanlydatsan.entity.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long courtId;
    private String courtName;
    private String venuesName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalPrice;
    private BookingStatus status;
}
