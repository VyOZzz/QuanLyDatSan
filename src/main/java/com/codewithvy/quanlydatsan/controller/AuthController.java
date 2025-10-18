package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.dto.ApiResponse;
import com.codewithvy.quanlydatsan.entity.*;
import com.codewithvy.quanlydatsan.exception.RoleNotFoundException;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.payload.request.ForgotPasswordRequest;
import com.codewithvy.quanlydatsan.payload.request.LoginRequest;
import com.codewithvy.quanlydatsan.payload.request.ResetPasswordRequest;
import com.codewithvy.quanlydatsan.payload.request.SignupRequest;
import com.codewithvy.quanlydatsan.payload.response.JwtResponse;
import com.codewithvy.quanlydatsan.repository.*;
import com.codewithvy.quanlydatsan.security.UserDetailsImpl;
import com.codewithvy.quanlydatsan.security.jwt.JwtUtils;
import com.codewithvy.quanlydatsan.service.EmailService;
import com.codewithvy.quanlydatsan.service.PasswordResetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;


    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder encoder,
                          JwtUtils jwtUtils,
                          PasswordResetService passwordResetService,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.passwordResetService = passwordResetService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getPhone(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        JwtResponse jwtResp = new JwtResponse(jwt, userDetails.getId(), userDetails.getPhone(), roles);
        return ResponseEntity.ok(ApiResponse.ok(jwtResp, "Login success"));
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Register request received: {}", signUpRequest);
        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Passwords do not match"));
        }
        if (userRepository.existsByPhone(signUpRequest.getPhone())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Phone is already in use"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Email is already in use"));
        }

        User user = new User();
        user.setFullname(signUpRequest.getFullname());
        user.setPhone(signUpRequest.getPhone());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setEmail(signUpRequest.getEmail());
        user.setUsername(signUpRequest.getEmail()); // dùng email làm username

        Set<Role> roles = new HashSet<>();
        String roleName = "OWNER".equalsIgnoreCase(signUpRequest.getAccountType()) ? "ROLE_OWNER" : "ROLE_USER";

        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName + " not found"));
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("User registered successfully", "Registered"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Email is required"));
        }
        // Tạo token (nếu email có tồn tại) và gửi – tránh lộ thông tin email tồn tại hay không.
        try {
            String token = passwordResetService.createTokenForEmail(request.getEmail());
            emailService.sendPlainText(request.getEmail(), "Password Reset",
                    "Mã đặt lại mật khẩu (token) của bạn: " + token + "\nToken hết hạn sau 15 phút.");
        } catch (ResourceNotFoundException ex) {
            // bỏ qua để tránh dò email
        }
        return ResponseEntity.ok(ApiResponse.ok("Nếu email hợp lệ, mã đặt lại đã được gửi", "Sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank() ||
            request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Token và mật khẩu mới là bắt buộc"));
        }
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công", "Password changed"));
    }
}
