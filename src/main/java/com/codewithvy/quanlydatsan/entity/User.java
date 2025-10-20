package com.codewithvy.quanlydatsan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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


    @Column(nullable = false)
    private String fullname; // họ tên hiển thị
    @Column(nullable = false, unique = true)
    private String phone; // số điện thoại duy nhất, dùng để login
    @Column(nullable = false)
    @JsonIgnore // Không trả password về client
    private String password; // mật khẩu đã mã hoá (BCrypt)
    @Column(nullable = false, unique = true)
    private String email; // email duy nhất, dùng cho forgot password
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private java.util.Set<Role> roles = new java.util.HashSet<>(); // danh sách quyền (ROLE_*) của user
}
