package com.codewithvy.quanlydatsan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @NotNull
    @Schema(description = "ID của venue", example = "1")
    private Long venueId; // ID của venues

    @NotNull
    @Schema(description = "ID của court cụ thể trong venue", example = "1")
    private Long courtId; // ID của court cụ thể trong venues

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Schema(
        description = "Thời gian bắt đầu đặt sân (Giờ Việt Nam - Asia/Ho_Chi_Minh)",
        example = "2025-11-07T14:00:00",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime startTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Schema(
        description = "Thời gian kết thúc đặt sân (Giờ Việt Nam - Asia/Ho_Chi_Minh)",
        example = "2025-11-07T15:00:00",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime endTime;
}
