package com.codewithvy.quanlydatsan.payload.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class LoginRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username length 3-50")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password length >= 6")
    private String password;
}
