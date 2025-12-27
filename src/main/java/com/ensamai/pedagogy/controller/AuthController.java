package com.ensamai.pedagogy.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null) {
            // Check authorities - order matters: most privileged first
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN"));
            boolean isTeacher = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("TEACHER"));

            if (isAdmin) {
                return "redirect:/admin/dashboard";
            } else if (isTeacher) {
                return "redirect:/teacher/dashboard";
            } else {
                return "redirect:/courses"; // Students go here
            }
        }
        return "redirect:/login";
    }
}