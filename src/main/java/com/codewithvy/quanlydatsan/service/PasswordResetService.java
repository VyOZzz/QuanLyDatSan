package com.codewithvy.quanlydatsan.service;

import com.codewithvy.quanlydatsan.entity.PasswordResetToken;
import com.codewithvy.quanlydatsan.entity.User;
import com.codewithvy.quanlydatsan.exception.ResourceNotFoundException;
import com.codewithvy.quanlydatsan.repository.PasswordResetTokenRepository;
import com.codewithvy.quanlydatsan.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String createTokenForEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email không tồn tại"));
        // generate random 32 bytes -> base64url ~ 43 chars
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        String tokenStr = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenStr);
        token.setUser(user);
        token.setExpiryDate(Instant.now().plus(15, ChronoUnit.MINUTES));
        tokenRepository.save(token);
        return tokenStr;
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResourceNotFoundException("Token không hợp lệ"));
        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new ResourceNotFoundException("Token đã hết hạn");
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        // lưu user tự động do persistence context hoặc explicit save
        userRepository.save(user);
        tokenRepository.delete(token); // một lần dùng
    }
}

