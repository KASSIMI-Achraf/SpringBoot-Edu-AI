package com.ensamai.pedagogy.config;

import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Role;
import com.ensamai.pedagogy.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create ADMIN
            if (appUserRepository.findByUsername("admin").isEmpty()) {
                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                appUserRepository.save(admin);
                System.out.println("✅ ADMIN Created: admin / admin123");
            }

            // Create STUDENT
            if (appUserRepository.findByUsername("student1").isEmpty()) {
                AppUser student = new AppUser();
                student.setUsername("student1");
                student.setPassword(passwordEncoder.encode("password"));
                student.setRole(Role.STUDENT);
                appUserRepository.save(student);
                System.out.println("✅ STUDENT Created: student1 / password");
            }
        };
    }
}