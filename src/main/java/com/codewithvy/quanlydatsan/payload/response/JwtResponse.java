package com.codewithvy.quanlydatsan.payload.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String jwtToken;
    private Long id;
    private String phone;
    private List<String> roles;
}
