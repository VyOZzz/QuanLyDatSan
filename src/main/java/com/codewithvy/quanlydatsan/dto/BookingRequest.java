package com.codewithvy.quanlydatsan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingRequest {
    @NotNull
    @Schema(description = "ID của venue", example = "1")
    private Long venueId; // ID của venues

    // ========== LEGACY FIELDS (Đặt 1 sân) - DEPRECATED ==========
    // Giữ lại để tương thích với frontend cũ
    @Schema(description = "[DEPRECATED] ID của court cụ thể - Sử dụng bookingItems để đặt nhiều sân", example = "1")
    private Long courtId; // ID của court cụ thể trong venues

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Schema(
        description = "[DEPRECATED] Thời gian bắt đầu - Sử dụng bookingItems để đặt nhiều sân",
        example = "2025-11-07T14:00:00",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @Schema(
        description = "[DEPRECATED] Thời gian kết thúc - Sử dụng bookingItems để đặt nhiều sân",
        example = "2025-11-07T15:00:00",
        type = "string",
        format = "date-time"
    )
    private LocalDateTime endTime;

    // ========== NEW FIELD (Đặt nhiều sân) ==========
    @Valid
    @Schema(
        description = "Danh sách các sân muốn đặt (có thể đặt nhiều sân cùng lúc). Nếu để trống, hệ thống sẽ dùng courtId, startTime, endTime (legacy mode)",
        example = "[{\"courtId\":1,\"startTime\":\"2025-11-07T14:00:00\",\"endTime\":\"2025-11-07T16:00:00\"},{\"courtId\":2,\"startTime\":\"2025-11-07T14:00:00\",\"endTime\":\"2025-11-07T16:00:00\"}]"
    )
    private List<BookingItemRequest> bookingItems;
}
