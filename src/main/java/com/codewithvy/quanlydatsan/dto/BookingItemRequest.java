package com.codewithvy.quanlydatsan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO để nhận thông tin đặt một sân cụ thể
 */
@Data
public class BookingItemRequest {

    @NotNull(message = "Court ID không được để trống")
    @Schema(description = "ID của sân cụ thể", example = "1")
    private Long courtId;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Schema(
        description = "Thời gian bắt đầu đặt sân (Giờ Việt Nam - Asia/Ho_Chi_Minh)",
        example = "2025-11-07T14:00:00",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Schema(
        description = "Thời gian kết thúc đặt sân (Giờ Việt Nam - Asia/Ho_Chi_Minh)",
        example = "2025-11-07T16:00:00",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime endTime;
}

