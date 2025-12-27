package com.ensamai.pedagogy.repository;

import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    
    // New method to list students for the dropdown menu
    List<AppUser> findByRole(Role role);
    long countByRole(Role role);
}