package com.ensamai.pedagogy.dto;

public class StudentAnalyticsDTO {
    private int totalQuizzes;
    private double averageScore;
    private double passRate;
    private int coursesEnrolled;

    public StudentAnalyticsDTO() {}

    public StudentAnalyticsDTO(int totalQuizzes, double averageScore, double passRate, int coursesEnrolled) {
        this.totalQuizzes = totalQuizzes;
        this.averageScore = averageScore;
        this.passRate = passRate;
        this.coursesEnrolled = coursesEnrolled;
    }

    // Getters and Setters
    public int getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(int totalQuizzes) { this.totalQuizzes = totalQuizzes; }
    
    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
    
    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
    
    public int getCoursesEnrolled() { return coursesEnrolled; }
    public void setCoursesEnrolled(int coursesEnrolled) { this.coursesEnrolled = coursesEnrolled; }
}