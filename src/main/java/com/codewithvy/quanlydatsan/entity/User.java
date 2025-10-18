package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity lưu thông tin người dùng hệ thống.
 */
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // định danh người dùng

    @Column(unique = true)
    private String username; // tên đăng nhập duy nhất
    @Column(nullable = false)
    private String fullname; // họ tên hiển thị
    @Column(nullable = false, unique = true)
    private String phone; // số điện thoại duy nhất
    @Column(nullable = false)
    private String password; // mật khẩu đã mã hoá (BCrypt)
    @Column(nullable = true, unique = true) // tạm cho phép null để migrate, có thể đổi lại false sau
    private String email; // email duy nhất

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private java.util.Set<Role> roles = new java.util.HashSet<>(); // danh sách quyền (ROLE_*) của user
}
