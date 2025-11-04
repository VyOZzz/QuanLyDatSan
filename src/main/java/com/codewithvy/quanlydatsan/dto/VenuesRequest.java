package com.codewithvy.quanlydatsan.dto;

import com.codewithvy.quanlydatsan.config.FlexibleLocalTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalTime;

@Data
public class VenuesRequest {
    @NotBlank(message = "Tên sân không được để trống")
    private String name;

    private String description;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @Email(message = "Email không hợp lệ")
    private String email;

    @NotNull(message = "Địa chỉ không được để trống")
    @Valid
    private AddressDTO address; // Nhập trực tiếp thông tin địa chỉ

    // Giá cố định theo giờ (VND)
    @Positive(message = "Giá theo giờ phải lớn hơn 0")
    private Double pricePerHour; // Optional khi update, bắt buộc khi create

    // Thời gian hoạt động
    @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
    @Schema(type = "string", example = "6 hoặc 06:00 hoặc 06:00:00", description = "Giờ mở cửa (hỗ trợ: 6, 06:00, 06:00:00)")
    private LocalTime openingTime;

    @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
    @Schema(type = "string", example = "23 hoặc 23:00 hoặc 23:00:00", description = "Giờ đóng cửa (hỗ trợ: 23, 23:00, 23:00:00)")
    private LocalTime closingTime;

    // Danh sách URL ảnh của venue
    @Schema(description = "Danh sách URL ảnh của venue", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private java.util.List<String> images;

    // Số lượng sân (tự động tạo courts khi khởi tạo venue)
    @Schema(description = "Số lượng sân muốn tạo (sẽ tự động tạo courts)", example = "5")
    private Integer numberOfCourt;
}
