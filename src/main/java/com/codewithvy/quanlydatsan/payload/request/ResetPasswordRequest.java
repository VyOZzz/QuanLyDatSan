package com.codewithvy.quanlydatsan.payload.request;

import lombok.Data;

/**
 * Payload đặt lại mật khẩu: client gửi token hợp lệ và mật khẩu mới.
 */
@Data
public class ResetPasswordRequest {
    private String token;       // token reset mật khẩu (được gửi qua email)
    private String newPassword; // mật khẩu mới muốn đặt
}
