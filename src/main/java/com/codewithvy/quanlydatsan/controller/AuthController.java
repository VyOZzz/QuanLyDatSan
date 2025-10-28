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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API xác thực và quản lý tài khoản người dùng")
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
    @Operation(
        summary = "Đăng nhập",
        description = "Xác thực người dùng bằng số điện thoại và mật khẩu, trả về JWT token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Đăng nhập thành công",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Số điện thoại hoặc mật khẩu không đúng"
        )
    })
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Kiểm tra user có tồn tại không trước
            User existingUser = userRepository.findByPhone(loginRequest.getPhone())
                    .orElse(null);

            if (existingUser == null) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.fail("Tài khoản không tồn tại. Vui lòng đăng ký trước khi đăng nhập"));
            }

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
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.fail("Số điện thoại hoặc mật khẩu không đúng"));
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.fail("Lỗi đăng nhập: " + ex.getMessage()));
        }
    }

    @PostMapping("/register")
    @Operation(
        summary = "Đăng ký tài khoản mới",
        description = "Tạo tài khoản người dùng mới với vai trò mặc định là ROLE_USER"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Đăng ký thành công"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Số điện thoại hoặc email đã tồn tại, hoặc mật khẩu không khớp"
        )
    })
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Register request received: {}", signUpRequest);

        // Kiểm tra mật khẩu khớp
        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
            logger.warn("Password mismatch for phone: {}", signUpRequest.getPhone());
            return ResponseEntity.badRequest().body(ApiResponse.fail("Passwords do not match"));
        }

        // Kiểm tra phone tồn tại - có log chi tiết
        boolean phoneExists = userRepository.existsByPhone(signUpRequest.getPhone());
        logger.info("Checking phone: {} - exists: {}", signUpRequest.getPhone(), phoneExists);

        if (phoneExists) {
            // Tìm user để log thông tin
            Optional<User> existingUser = userRepository.findByPhone(signUpRequest.getPhone());
            if (existingUser.isPresent()) {
                logger.warn("Phone {} already exists - User ID: {}, Email: {}",
                    signUpRequest.getPhone(), existingUser.get().getId(), existingUser.get().getEmail());
            } else {
                logger.error("Phone {} exists but findByPhone returns empty - possible data inconsistency!",
                    signUpRequest.getPhone());
            }
            return ResponseEntity.badRequest().body(ApiResponse.fail("Phone is already in use"));
        }

        // Kiểm tra email tồn tại - có log chi tiết
        boolean emailExists = userRepository.existsByEmail(signUpRequest.getEmail());
        logger.info("Checking email: {} - exists: {}", signUpRequest.getEmail(), emailExists);

        if (emailExists) {
            Optional<User> existingUser = userRepository.findByEmail(signUpRequest.getEmail());
            if (existingUser.isPresent()) {
                logger.warn("Email {} already exists - User ID: {}, Phone: {}",
                    signUpRequest.getEmail(), existingUser.get().getId(), existingUser.get().getPhone());
            } else {
                logger.error("Email {} exists but findByEmail returns empty - possible data inconsistency!",
                    signUpRequest.getEmail());
            }
            return ResponseEntity.badRequest().body(ApiResponse.fail("Email is already in use"));
        }

        logger.info("Creating new user with phone: {} and email: {}",
            signUpRequest.getPhone(), signUpRequest.getEmail());

        User user = new User();
        user.setFullname(signUpRequest.getFullname());
        user.setPhone(signUpRequest.getPhone());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setEmail(signUpRequest.getEmail());
        // Bỏ setUsername vì đã xóa field username

        // MẶC ĐỊNH TẤT CẢ USER MỚI ĐỀU LÀ ROLE_USER
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("ROLE_USER not found"));
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully - ID: {}, Phone: {}", savedUser.getId(), savedUser.getPhone());

        return ResponseEntity.ok(ApiResponse.ok("User registered successfully", "Registered"));
    }

    @PostMapping("/forgot-password")
    @Operation(
        summary = "Quên mật khẩu",
        description = "Gửi mã đặt lại mật khẩu qua email"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Nếu email hợp lệ, mã đặt lại đã được gửi"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Email không hợp lệ"
        )
    })
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
    @Operation(
        summary = "Đặt lại mật khẩu",
        description = "Đặt lại mật khẩu sử dụng token nhận được qua email"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Đổi mật khẩu thành công"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Token không hợp lệ hoặc đã hết hạn"
        )
    })
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank() ||
            request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Token và mật khẩu mới là bắt buộc"));
        }
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Đổi mật khẩu thành công", "Password changed"));
    }
}
