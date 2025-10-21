package com.codewithvy.quanlydatsan.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class PriceRuleRequest {
    private Long venueId;
    private String name; // Tên khung giờ (VD: "Giờ cao điểm buổi sáng")
    private LocalTime startTime; // Giờ bắt đầu
    private LocalTime endTime; // Giờ kết thúc
    private Double pricePerHour; // Giá theo giờ (VND)
}
