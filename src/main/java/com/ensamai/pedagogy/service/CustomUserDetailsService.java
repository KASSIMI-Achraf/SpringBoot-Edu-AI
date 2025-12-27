package com.ensamai.pedagogy.service;

import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Check if account is active
        if (!appUser.isActive()) {
            throw new UsernameNotFoundException("Account is deactivated: " + username);
        }

        // Returns the user with the authority (ADMIN, TEACHER, or STUDENT)
        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .authorities(appUser.getRole().name()) 
                .build();
    }
}