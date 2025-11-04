package com.codewithvy.quanlydatsan.dto;

import lombok.Data;

/**
 * DTO cho việc cập nhật thông tin user
 */
@Data
public class UpdateUserRequest {
    private String fullname;
    private String email;
    private String phone;  // Số điện thoại

    // Thông tin ngân hàng (chỉ OWNER cần)
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
}

