package com.codewithvy.quanlydatsan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

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

    // Danh sách quy tắc giá (optional, chỉ dùng khi muốn cập nhật giá)
    @Valid
    private List<PriceRuleRequest> priceRules;
}
