package com.ensamai.pedagogy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score; // e.g., 85 (out of 100)
    
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private AppUser student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    public QuizResult() {
        this.completedAt = LocalDateTime.now();
    }

    public QuizResult(AppUser student, Course course, int score) {
        this.student = student;
        this.course = course;
        this.score = score;
        this.completedAt = LocalDateTime.now();
    }

    // --- GETTERS AND SETTERS ---
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    // FIXED: Added this method so the Service can call it
    public int getScorePercentage() {
        return this.score; 
    }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public AppUser getStudent() { return student; }
    public void setStudent(AppUser student) { this.student = student; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}