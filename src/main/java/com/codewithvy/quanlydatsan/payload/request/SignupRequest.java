package com.codewithvy.quanlydatsan.payload.request;
import lombok.Data;
import java.util.Set;

@Data
public class SignupRequest {
    private String fullname;
    private String phone;
    private String username;
    private String password;
    private Set<String> roles;
}

