package com.ensamai.pedagogy.config;

import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Role;
import com.ensamai.pedagogy.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create ADMIN (super admin - only one allowed, cannot be deleted)
            if (!appUserRepository.existsByRole(Role.ADMIN)) {
                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("System Administrator");
                admin.setRole(Role.ADMIN);
                admin.setActive(true);
                admin.setDeletable(false); // Admin cannot be deleted
                admin.setCreatedAt(LocalDateTime.now());
                appUserRepository.save(admin);
                System.out.println("✅ ADMIN Created: admin@gmail.com / admin123");
            }

            // Create sample TEACHER
            if (appUserRepository.findByUsername("teacher1").isEmpty()) {
                AppUser teacher = new AppUser();
                teacher.setUsername("teacher1");
                teacher.setEmail("teacher1@gmail.com");
                teacher.setPassword(passwordEncoder.encode("password"));
                teacher.setFullName("John Teacher");
                teacher.setRole(Role.TEACHER);
                teacher.setActive(true);
                teacher.setDeletable(true);
                teacher.setCreatedAt(LocalDateTime.now());
                // Find admin and set as creator
                appUserRepository.findByRole(Role.ADMIN).stream().findFirst()
                    .ifPresent(teacher::setCreatedBy);
                appUserRepository.save(teacher);
                System.out.println("✅ TEACHER Created: teacher1@gmail.com / password");
            }

            // Create sample STUDENT
            if (appUserRepository.findByUsername("student1").isEmpty()) {
                AppUser student = new AppUser();
                student.setUsername("student1");
                student.setEmail("student1@gmail.com");
                student.setPassword(passwordEncoder.encode("password"));
                student.setFullName("Jane Student");
                student.setRole(Role.STUDENT);
                student.setActive(true);
                student.setDeletable(true);
                student.setCreatedAt(LocalDateTime.now());
                appUserRepository.save(student);
                System.out.println("✅ STUDENT Created: student1@gmail.com / password");
            }
        };
    }
}