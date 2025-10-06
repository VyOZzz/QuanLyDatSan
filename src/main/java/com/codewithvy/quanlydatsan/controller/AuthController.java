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

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;
    private final AddressRepository addressRepository;
    private final VenuesRepository venuesRepository;
    private final VenuesDetailRepository venuesDetailRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder encoder,
                          JwtUtils jwtUtils,
                          PasswordResetService passwordResetService,
                          EmailService emailService,
                          AddressRepository addressRepository,
                          VenuesRepository venuesRepository,
                          VenuesDetailRepository venuesDetailRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.passwordResetService = passwordResetService;
        this.emailService = emailService;
        this.addressRepository = addressRepository;
        this.venuesRepository = venuesRepository;
        this.venuesDetailRepository = venuesDetailRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        JwtResponse jwtResp = new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), roles);
        return ResponseEntity.ok(ApiResponse.ok(jwtResp, "Login success"));
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<ApiResponse<String>> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Username is already taken"));
        }
        if (userRepository.existsByPhone(signUpRequest.getPhone())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Phone is already in use"));
        }
        if (signUpRequest.getEmail() == null || signUpRequest.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Email is required"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Email is already in use"));
        }
        User user = new User();
        user.setFullname(signUpRequest.getFullname());
        user.setPhone(signUpRequest.getPhone());
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setEmail(signUpRequest.getEmail());

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        boolean isOwner = false;
        if (signUpRequest.getAccountType() != null && signUpRequest.getAccountType().equalsIgnoreCase("OWNER")) {
            isOwner = true;
        }

        if (strRoles == null || strRoles.isEmpty()) {
            String defaultRoleName = isOwner ? "ROLE_OWNER" : "ROLE_USER";
            Role defaultRole = roleRepository.findByName(defaultRoleName)
                    .orElseThrow(() -> new RoleNotFoundException(defaultRoleName + " not found"));
            roles.add(defaultRole);
        } else {
            for (String roleName : strRoles) { // replaced lambda to allow mutation logic
                Role foundRole = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RoleNotFoundException(roleName + " not found"));
                roles.add(foundRole);
                if ("ROLE_OWNER".equalsIgnoreCase(roleName)) {
                    isOwner = true; // safe mutation in loop
                }
            }
        }
        user.setRoles(roles);
        userRepository.save(user);

        // If owner, create venues data immediately (optional logic)
        if (isOwner) {
            // Validate required venue fields
            if (signUpRequest.getVenueName() == null || signUpRequest.getVenueProvinceOrCity() == null ||
                signUpRequest.getVenueDistrict() == null || signUpRequest.getVenueDetailAddress() == null) {
                throw new ResourceNotFoundException("Missing venue address fields for OWNER account");
            }
            Address address = new Address();
            address.setProvinceOrCity(signUpRequest.getVenueProvinceOrCity());
            address.setDistrict(signUpRequest.getVenueDistrict());
            address.setDetailAddress(signUpRequest.getVenueDetailAddress());
            addressRepository.save(address);

            Venues venues = new Venues();
            venues.setName(signUpRequest.getVenueName());
            venues.setNumberOfCourt(signUpRequest.getVenueNumberOfCourt() == null ? 0 : signUpRequest.getVenueNumberOfCourt());
            venues.setAddress(address);
            venuesRepository.save(venues);

            if (signUpRequest.getVenueTitle() != null && signUpRequest.getVenueDescription() != null) {
                VenuesDetail detail = new VenuesDetail();
                detail.setTitle(signUpRequest.getVenueTitle());
                detail.setDescription(signUpRequest.getVenueDescription());
                detail.setVenues(venues);
                venuesDetailRepository.save(detail);
            }
        }

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
