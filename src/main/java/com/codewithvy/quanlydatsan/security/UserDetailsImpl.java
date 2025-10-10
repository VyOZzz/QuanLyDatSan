package com.codewithvy.quanlydatsan.security;

import com.codewithvy.quanlydatsan.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai UserDetails để Spring Security sử dụng trong SecurityContext.
 * Lưu trữ các thuộc tính cần thiết (id, username, password, authorities) của người dùng đã xác thực.
 */
public class UserDetailsImpl implements UserDetails {
    private Long id; // id người dùng trong hệ thống
    private String username; // tên đăng nhập
    private String password; // mật khẩu đã mã hoá
    private Collection<? extends GrantedAuthority> authorities; // danh sách quyền (ROLE_*)

    public UserDetailsImpl(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Tạo UserDetailsImpl từ entity User: map các Role thành GrantedAuthority.
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return username; }

    public Long getId() { return id; }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
