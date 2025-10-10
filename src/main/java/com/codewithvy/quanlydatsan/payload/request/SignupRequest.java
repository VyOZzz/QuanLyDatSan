package com.codewithvy.quanlydatsan.payload.request;

import lombok.Data;
import java.util.Set;
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

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "Phone must be digits 8-15 length")
    private String phone; // số điện thoại duy nhất

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username length 3-50")
    private String username; // tên đăng nhập duy nhất

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password length >=6")
    private String password; // mật khẩu (sẽ được mã hoá khi lưu)

    @NotBlank(message = "Email is required")
    @Email(message = "Email invalid")
    private String email; // email duy nhất

    @Pattern(regexp = "(?i)^(USER|OWNER)$", message = "accountType must be USER or OWNER")
    private String accountType; // USER hoặc OWNER

    private Set<String> roles; // roles tuỳ chọn, nếu không truyền sẽ lấy mặc định theo accountType

    // Thông tin venues nếu là OWNER (validate thủ công trong controller)
    private String venueName; // tên địa điểm
    private Integer venueNumberOfCourt; // số sân đăng ký
    private String venueProvinceOrCity; // tỉnh/thành phố
    private String venueDistrict; // quận/huyện
    private String venueDetailAddress; // địa chỉ chi tiết
    private String venueTitle; // tiêu đề phần mô tả venues detail
    private String venueDescription; // mô tả chi tiết venues
}
