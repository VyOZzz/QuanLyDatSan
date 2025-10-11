package com.codewithvy.quanlydatsan.config;

import com.codewithvy.quanlydatsan.entity.Role;
import com.codewithvy.quanlydatsan.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            // Ensure ROLE_USER exists
            roleRepository.findByName("ROLE_USER").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_USER");
                return roleRepository.save(r);
            });
            // Ensure ROLE_OWNER exists
            roleRepository.findByName("ROLE_OWNER").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_OWNER");
                return roleRepository.save(r);
            });
        };
    }
}
