package com.codewithvy.quanlydatsan.payload.request;

import lombok.Data;
import java.util.Set;
import jakarta.validation.constraints.*;

@Data
public class SignupRequest {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name length 2-100")
    private String fullname;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "Phone must be digits 8-15 length")
    private String phone;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username length 3-50")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password length >=6")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email invalid")
    private String email; // mới

    @Pattern(regexp = "(?i)^(USER|OWNER)$", message = "accountType must be USER or OWNER")
    private String accountType; // USER hoặc OWNER

    private Set<String> roles; // vẫn giữ để linh hoạt

    // Thông tin sân nếu là chủ sân (validate thủ công trong controller khi accountType=OWNER)
    private String venueName;
    private Integer venueNumberOfCourt;
    private String venueProvinceOrCity;
    private String venueDistrict;
    private String venueDetailAddress;
    private String venueTitle; // venues detail
    private String venueDescription;
}
