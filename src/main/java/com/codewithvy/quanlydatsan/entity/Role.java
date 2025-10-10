package com.codewithvy.quanlydatsan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity lưu quyền (role) của người dùng, ví dụ: ROLE_USER, ROLE_OWNER, ROLE_ADMIN.
 */
@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // id role

    @Column(nullable = false, unique = true)
    private String name; // tên role duy nhất (ROLE_*)
}
