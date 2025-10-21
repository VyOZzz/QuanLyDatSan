package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.dto.UpdateUserRequest;
import com.codewithvy.quanlydatsan.dto.UserDTO;
import com.codewithvy.quanlydatsan.entity.User;
import com.codewithvy.quanlydatsan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý thông tin user và nâng cấp vai trò
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * API "Trở thành chủ sân" - nâng cấp từ USER lên OWNER
     * Chỉ cho phép người dùng đã đăng nhập với role USER gọi API này
     */
    @PostMapping("/me/request-owner-role")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> requestOwnerRole() {
        try {
            User currentUser = userService.getCurrentUser();
            userService.addOwnerRole(currentUser);
            return ResponseEntity.ok(
                ApiResponse.ok("Đã nâng cấp thành chủ sân thành công! Vui lòng đăng nhập l��i để cập nhật quyền.", "Success")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(e.getMessage()));
        }
    }

    /**
     * API lấy thông tin user hiện tại
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        User user = userService.getCurrentUser();
        UserDTO userDTO = UserDTO.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.ok(userDTO, "Success"));
    }

    /**
     * API cập nhật thông tin user (bao gồm thông tin ngân hàng)
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUser(@RequestBody UpdateUserRequest request) {
        try {
            User updatedUser = userService.updateCurrentUser(request);
            UserDTO userDTO = UserDTO.fromEntity(updatedUser);
            return ResponseEntity.ok(ApiResponse.ok(userDTO, "Cập nhật thông tin thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(e.getMessage()));
        }
    }
}
