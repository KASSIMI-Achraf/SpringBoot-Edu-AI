package com.ensamai.pedagogy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables the @PreAuthorize annotation
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Role hierarchy: ADMIN > TEACHER > STUDENT
     * Admin can do everything a teacher can do.
     * Teacher can do everything a student can do.
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ADMIN > TEACHER\nTEACHER > STUDENT");
        return hierarchy;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in dev
            .authorizeHttpRequests(auth -> auth
                // 1. Allow Public Access to Login, Error pages, and Static Resources
                .requestMatchers("/login", "/error", "/h2-console/**", "/css/**", "/js/**", "/images/**").permitAll()
                
                // 2. Admin-only endpoints
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                
                // 3. Teacher endpoints (admin can also access due to hierarchy)
                .requestMatchers("/teacher/**").hasAnyAuthority("ADMIN", "TEACHER")
                
                // 4. All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true) // Redirect to Root (AuthController will handle routing)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // Allow H2 Console

        return http.build();
    }
}