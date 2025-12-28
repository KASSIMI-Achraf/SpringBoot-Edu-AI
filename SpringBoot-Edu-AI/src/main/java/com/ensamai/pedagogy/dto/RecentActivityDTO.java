package com.ensamai.pedagogy.dto;

import java.time.LocalDateTime;

public class RecentActivityDTO {
    private String studentName;
    private String courseName;
    private double score;
    private boolean passed;
    private LocalDateTime completedAt;

    public RecentActivityDTO() {}

    public RecentActivityDTO(String studentName, String courseName, double score, boolean passed, LocalDateTime completedAt) {
        this.studentName = studentName;
        this.courseName = courseName;
        this.score = score;
        this.passed = passed;
        this.completedAt = completedAt;
    }

    // Getters and Setters
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}