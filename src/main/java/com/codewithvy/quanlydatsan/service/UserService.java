package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.entity.Role;
import com.codewithvy.quanlydatsan.entity.User;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.exception.RoleNotFoundException;
import com.codewithvy.quanlydatsan.repository.RoleRepository;
import com.codewithvy.quanlydatsan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Thêm role OWNER cho user (nâng cấp thành chủ s��n)
     */
    @Transactional
    public void addOwnerRole(User user) {
        // Kiểm tra xem user đã có role OWNER chưa
        boolean hasOwnerRole = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_OWNER"));

        if (hasOwnerRole) {
            throw new RuntimeException("Bạn đã là chủ sân rồi!");
        }

        // Tìm role OWNER trong database
        Role ownerRole = roleRepository.findByName("ROLE_OWNER")
                .orElseThrow(() -> new RoleNotFoundException("ROLE_OWNER not found"));

        // Thêm role OWNER vào danh sách roles của user (KHÔNG XÓA role cũ)
        user.getRoles().add(ownerRole);
        userRepository.save(user);
    }
}

