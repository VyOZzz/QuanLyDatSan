package com.codewithvy.quanlydatsan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.codewithvy.quanlydatsan.entity.Role;
import com.codewithvy.quanlydatsan.repository.RoleRepository;

import java.util.TimeZone;

/**
 * Điểm vào chính của ứng dụng Spring Boot (hàm main chạy app).
 * Có kèm CommandLineRunner để seed một số role mặc định vào DB nếu chưa có.
 */
@SpringBootApplication
@EnableScheduling
public class QuanLyDatSanApplication {

    public static void main(String[] args) {
        // Đặt múi giờ mặc định cho toàn bộ JVM là Việt Nam (UTC+7)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        SpringApplication.run(QuanLyDatSanApplication.class, args);
    }

    /**
     * Seed các role mặc định (ROLE_USER, ROLE_OWNER, ROLE_ADMIN) khi app khởi động lần đầu.
     */
    @Bean
    public CommandLineRunner initRoles(@Autowired RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName("ROLE_USER").isEmpty()) {
                Role userRole = new Role();
                userRole.setName("ROLE_USER");
                roleRepository.save(userRole);
            }
            // Seed ROLE_OWNER để phục vụ đăng ký tài khoản chủ sân
            if (roleRepository.findByName("ROLE_OWNER").isEmpty()) {
                Role ownerRole = new Role();
                ownerRole.setName("ROLE_OWNER");
                roleRepository.save(ownerRole);
            }
            if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
                Role adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                roleRepository.save(adminRole);
            }
        };
    }
}
