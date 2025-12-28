package com.ensamai.pedagogy.repository;

import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    
    // Find users by role
    List<AppUser> findByRole(Role role);
    long countByRole(Role role);
    
    // Check if an admin exists
    boolean existsByRole(Role role);
    
    // Find active users by role
    List<AppUser> findByRoleAndActive(Role role, boolean active);
    
    // Find non-deletable user (the admin)
    Optional<AppUser> findByRoleAndIsDeletable(Role role, boolean isDeletable);
    
    // Count active users by role
    long countByRoleAndActive(Role role, boolean active);
}