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

@Service
public class TeacherService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


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


    @Transactional
    public void deactivateTeacher(Long id) {
        AppUser teacher = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + id));
        
        validateNotAdmin(teacher);
        teacher.setActive(false);
        appUserRepository.save(teacher);
    }

    @Transactional
    public void activateTeacher(Long id) {
        AppUser teacher = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + id));
        
        validateNotAdmin(teacher);
        teacher.setActive(true);
        appUserRepository.save(teacher);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        AppUser teacher = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + id));
        
        validateNotAdmin(teacher);

        if (!teacher.isDeletable()) {
            throw new IllegalStateException("This account cannot be deleted.");
        }

       
        long courseCount = courseRepository.countByTeacherId(id);
        if (courseCount > 0) {
            throw new IllegalStateException("Cannot delete teacher with " + courseCount + " assigned courses. Reassign courses first.");
        }

        appUserRepository.delete(teacher);
    }


    public List<AppUser> getAllTeachers() {
        return appUserRepository.findByRole(Role.TEACHER);
    }

    public List<AppUser> getActiveTeachers() {
        return appUserRepository.findByRoleAndActive(Role.TEACHER, true);
    }

    public AppUser getTeacherById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + id));
    }


    public long countTeachers() {
        return appUserRepository.countByRole(Role.TEACHER);
    }

    public long countActiveTeachers() {
        return appUserRepository.countByRoleAndActive(Role.TEACHER, true);
    }

    private void validateNotAdmin(AppUser user) {
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Cannot modify admin account through teacher management.");
        }
    }

    public boolean adminExists() {
        return appUserRepository.existsByRole(Role.ADMIN);
    }
}
