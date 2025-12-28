package com.ensamai.pedagogy.controller;

import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/student") // Changed URL prefix to match folder logic
public class ProfileController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Show the Change Password Page
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        // Now looks inside templates/student/ folder
        return "student/password_change"; 
    }

    // 2. Process the Password Change
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword, 
                                 @RequestParam String confirmPassword, 
                                 Model model) {
        
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "student/password_change";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);

        return "redirect:/logout"; 
    }
}