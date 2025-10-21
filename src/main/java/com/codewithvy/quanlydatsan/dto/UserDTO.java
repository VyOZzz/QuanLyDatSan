package com.codewithvy.quanlydatsan.dto;

import com.codewithvy.quanlydatsan.entity.Role;
import com.codewithvy.quanlydatsan.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO trả về thông tin user (không có password)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String fullname;
    private String phone;
    private String email;
    private Set<String> roles;

    // Thông tin ngân hàng
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;

    /**
     * Convert từ User entity sang UserDTO
     */
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .bankName(user.getBankName())
                .bankAccountNumber(user.getBankAccountNumber())
                .bankAccountName(user.getBankAccountName())
                .build();
    }
}

