package com.ensamai.pedagogy.dto;

public class AdminAnalyticsDTO {
    private int totalStudents;
    private int totalQuizzesTaken;
    private double classAverageScore;
    private double overallPassRate;

    public AdminAnalyticsDTO() {}

    public AdminAnalyticsDTO(int totalStudents, int totalQuizzesTaken, double classAverageScore, double overallPassRate) {
        this.totalStudents = totalStudents;
        this.totalQuizzesTaken = totalQuizzesTaken;
        this.classAverageScore = classAverageScore;
        this.overallPassRate = overallPassRate;
    }

    // Getters and Setters
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    
    public int getTotalQuizzesTaken() { return totalQuizzesTaken; }
    public void setTotalQuizzesTaken(int totalQuizzesTaken) { this.totalQuizzesTaken = totalQuizzesTaken; }
    
    public double getClassAverageScore() { return classAverageScore; }
    public void setClassAverageScore(double classAverageScore) { this.classAverageScore = classAverageScore; }
    
    public double getOverallPassRate() { return overallPassRate; }
    public void setOverallPassRate(double overallPassRate) { this.overallPassRate = overallPassRate; }
}
