package com.codewithvy.quanlydatsan.payload.request;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * Payload đăng ký tài khoản mới. Có thể đăng ký kiểu USER hoặc OWNER.
 * Nếu là OWNER, có thể truyền kèm thông tin venues để tạo sẵn.
 */
@Data
public class SignupRequest {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name length 2-100")
    private String fullname; // họ tên đầy đủ

    @NotBlank(message = "Email is required")
    @Email(message = "Email invalid")
    private String email; // email duy nhất

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "Phone must be digits 8-15 length")
    private String phone; // số điện thoại duy nhất

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password length >=6")
    private String password; // mật khẩu (sẽ được mã hoá khi lưu)

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "(?i)^(USER|OWNER)$", message = "accountType must be USER or OWNER")
    private String accountType;
}
