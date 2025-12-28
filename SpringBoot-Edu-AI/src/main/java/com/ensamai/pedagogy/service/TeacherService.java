package com.ensamai.pedagogy.service;

import com.ensamai.pedagogy.dto.TeacherDTO;
import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Role;
import com.ensamai.pedagogy.repository.AppUserRepository;
import com.ensamai.pedagogy.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing teacher accounts (admin-only operations)
 */
@Service
public class TeacherService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create a new teacher account
     */
    @Transactional
    public AppUser createTeacher(TeacherDTO dto, AppUser createdBy) {
        // Validate username uniqueness
        if (appUserRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }

        // Validate email uniqueness
        if (appUserRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        AppUser teacher = new AppUser();
        teacher.setUsername(dto.getUsername());
        teacher.setEmail(dto.getEmail());
        teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
        teacher.setFullName(dto.getFullName());
        teacher.setRole(Role.TEACHER);
        teacher.setActive(true);
        teacher.setDeletable(true);
        teacher.setCreatedBy(createdBy);
        teacher.setCreatedAt(LocalDateTime.now());

        return appUserRepository.save(teacher);
    }

    /**
     * Update an existing teacher's details
     */
    @Transactional
    public AppUser updateTeacher(Long id, TeacherDTO dto) {
        AppUser teacher = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + id));

        validateNotAdmin(teacher);

        // Check username uniqueness (if changed)
        if (!teacher.getUsername().equals(dto.getUsername())) {
            if (appUserRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
            }
            teacher.setUsername(dto.getUsername());
        }

        // Check email uniqueness (if changed)
        if (!teacher.getEmail().equals(dto.getEmail())) {
            if (appUserRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
            }
            teacher.setEmail(dto.getEmail());
        }

        teacher.setFullName(dto.getFullName());

        // Only update password if provided
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return appUserRepository.save(teacher);
    }

    /**
     * Deactivate a teacher account (soft delete)
     */
    @Transactional
    public void deactivateTeacher(Long id) {
        AppUser teacher = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + id));
        
        validateNotAdmin(teacher);
        teacher.setActive(false);
        appUserRepository.save(teacher);
    }

    /**
     * Activate a previously deactivated teacher account
     */
    @Transactional
    public void activateTeacher(Long id) {
        AppUser teacher = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + id));
        
        validateNotAdmin(teacher);
        teacher.setActive(true);
        appUserRepository.save(teacher);
    }

    /**
     * Permanently delete a teacher (only if deletable)
     */
    @Transactional
    public void deleteTeacher(Long id) {
        AppUser teacher = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + id));
        
        validateNotAdmin(teacher);

        if (!teacher.isDeletable()) {
            throw new IllegalStateException("This account cannot be deleted.");
        }

        // Check if teacher has courses - need to reassign or handle them
        long courseCount = courseRepository.countByTeacherId(id);
        if (courseCount > 0) {
            throw new IllegalStateException("Cannot delete teacher with " + courseCount + " assigned courses. Reassign courses first.");
        }

        appUserRepository.delete(teacher);
    }

    /**
     * Get all teachers (both active and inactive)
     */
    public List<AppUser> getAllTeachers() {
        return appUserRepository.findByRole(Role.TEACHER);
    }

    /**
     * Get only active teachers
     */
    public List<AppUser> getActiveTeachers() {
        return appUserRepository.findByRoleAndActive(Role.TEACHER, true);
    }

    /**
     * Get a single teacher by ID
     */
    public AppUser getTeacherById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + id));
    }

    /**
     * Count total teachers
     */
    public long countTeachers() {
        return appUserRepository.countByRole(Role.TEACHER);
    }

    /**
     * Count active teachers
     */
    public long countActiveTeachers() {
        return appUserRepository.countByRoleAndActive(Role.TEACHER, true);
    }

    /**
     * Validate that the user is not an admin (prevent admin modifications)
     */
    private void validateNotAdmin(AppUser user) {
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Cannot modify admin account through teacher management.");
        }
    }

    /**
     * Check if admin already exists in the system
     */
    public boolean adminExists() {
        return appUserRepository.existsByRole(Role.ADMIN);
    }
}
