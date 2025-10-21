package com.codewithvy.quanlydatsan.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @NotNull
    private Long venueId; // ID của venues

    @NotNull
    private Long courtId; // ID của court cụ thể trong venues

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;
}
