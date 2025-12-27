package com.ensamai.pedagogy.dto;

/**
 * DTO for system-wide analytics visible to admin
 */
public class SystemAnalyticsDTO {
    private int totalTeachers;
    private int totalStudents;
    private int totalCourses;
    private int totalQuizzesTaken;
    private double systemAverageScore;
    private double overallPassRate;

    public SystemAnalyticsDTO() {}

    public SystemAnalyticsDTO(int totalTeachers, int totalStudents, int totalCourses,
                              int totalQuizzesTaken, double systemAverageScore, double overallPassRate) {
        this.totalTeachers = totalTeachers;
        this.totalStudents = totalStudents;
        this.totalCourses = totalCourses;
        this.totalQuizzesTaken = totalQuizzesTaken;
        this.systemAverageScore = systemAverageScore;
        this.overallPassRate = overallPassRate;
    }

    // Getters and Setters
    public int getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(int totalTeachers) { this.totalTeachers = totalTeachers; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getTotalCourses() { return totalCourses; }
    public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }

    public int getTotalQuizzesTaken() { return totalQuizzesTaken; }
    public void setTotalQuizzesTaken(int totalQuizzesTaken) { this.totalQuizzesTaken = totalQuizzesTaken; }

    public double getSystemAverageScore() { return systemAverageScore; }
    public void setSystemAverageScore(double systemAverageScore) { this.systemAverageScore = systemAverageScore; }

    public double getOverallPassRate() { return overallPassRate; }
    public void setOverallPassRate(double overallPassRate) { this.overallPassRate = overallPassRate; }
}
