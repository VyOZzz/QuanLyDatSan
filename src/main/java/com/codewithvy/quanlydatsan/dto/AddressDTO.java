package com.codewithvy.quanlydatsan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho địa chỉ khi tạo/cập nhật Venue
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;                // ID địa chỉ (chỉ dùng khi trả về)

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String provinceOrCity;  // Tỉnh/Thành phố

    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;        // Quận/Huyện

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String detailAddress;   // Địa chỉ chi tiết (số nhà, đường)
}
