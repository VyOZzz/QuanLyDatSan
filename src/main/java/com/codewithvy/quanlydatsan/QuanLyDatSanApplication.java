package com.codewithvy.quanlydatsan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import com.codewithvy.quanlydatsan.entity.Role;
import com.codewithvy.quanlydatsan.repository.RoleRepository;

@SpringBootApplication
public class QuanLyDatSanApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuanLyDatSanApplication.class, args);
    }

    @Bean
    public CommandLineRunner initRoles(@Autowired RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName("ROLE_USER").isEmpty()) {
                Role userRole = new Role();
                userRole.setName("ROLE_USER");
                roleRepository.save(userRole);
            }
            if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
                Role adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                roleRepository.save(adminRole);
            }
        };
    }
}
