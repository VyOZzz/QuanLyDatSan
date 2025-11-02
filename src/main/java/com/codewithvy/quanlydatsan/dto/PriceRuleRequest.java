package com.codewithvy.quanlydatsan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalTime;

@Data
public class PriceRuleRequest {
    private Long venueId;

    @Schema(description = "Tên khung giờ", example = "Giờ cao điểm buổi sáng")
    private String name; // Tên khung giờ (VD: "Giờ cao điểm buổi sáng")

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(type = "string", pattern = "HH:mm:ss", example = "06:00:00", description = "Giờ bắt đầu (format: HH:mm:ss)")
    private LocalTime startTime; // Giờ bắt đầu

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(type = "string", pattern = "HH:mm:ss", example = "18:00:00", description = "Giờ kết thúc (format: HH:mm:ss)")
    private LocalTime endTime; // Giờ kết thúc

    @Schema(description = "Giá theo giờ (VND)", example = "150000")
    private Double pricePerHour; // Giá theo giờ (VND)
}
