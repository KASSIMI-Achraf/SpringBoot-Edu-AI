package com.ensamai.pedagogy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Account status - can be deactivated by admin
    private boolean active = true;

    // Admin account cannot be deleted
    private boolean isDeletable = true;

    // Who created this account (for teachers created by admin)
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private AppUser createdBy;

    private LocalDateTime createdAt;

    // Relationship: One Student has Many Courses
    @ManyToMany(mappedBy = "students")
    private List<Course> courses = new ArrayList<>();

    // Constructors
    public AppUser() {
        this.createdAt = LocalDateTime.now();
    }
    
    public AppUser(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true;
        this.isDeletable = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isDeletable() { return isDeletable; }
    public void setDeletable(boolean isDeletable) { this.isDeletable = isDeletable; }

    public AppUser getCreatedBy() { return createdBy; }
    public void setCreatedBy(AppUser createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }

    // Helper method to check if user is admin
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    // Helper method to check if user is teacher
    public boolean isTeacher() {
        return this.role == Role.TEACHER;
    }

    // Helper method to check if user is student
    public boolean isStudent() {
        return this.role == Role.STUDENT;
    }
}