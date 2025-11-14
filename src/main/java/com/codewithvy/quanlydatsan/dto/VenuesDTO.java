package com.codewithvy.quanlydatsan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenuesDTO {
    private Long id;
    private String name;
    private String description; // mô tả về venues
    private int numberOfCourt;
    private AddressDTO address;
    private Integer courtsCount; // tránh load toàn bộ courts
    private Double pricePerHour; // Giá cố định theo giờ (VND)

    // Thông tin đánh giá
    private Double averageRating; // Điểm trung bình (1-5 sao)
    private Integer totalReviews; // Tổng số đánh giá

    // Thời gian hoạt động
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime openingTime; // Giờ mở cửa

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime closingTime; // Giờ đóng cửa

    // Danh sách ảnh của venue
    private java.util.List<String> images;

    // Thông tin chủ sân
    private String ownerPhoneNumber; // Số điện thoại chủ sân
}
