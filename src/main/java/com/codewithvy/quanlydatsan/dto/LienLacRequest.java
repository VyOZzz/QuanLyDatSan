package com.codewithvy.quanlydatsan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo liên lạc mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LienLacRequest {

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @NotBlank(message = "Chủ đề không được để trống")
    @Size(max = 200, message = "Chủ đề không được vượt quá 200 ký tự")
    private String subject;

    @NotBlank(message = "Nội dung không được để trống")
    private String message;

    // Optional: ID của venue nếu liên hệ về một sân cụ thể
    private Long venueId;
}
