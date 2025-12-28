package com.ensamai.pedagogy.dto;

public class CourseDataPoint {
    private String courseName;
    private double avgScore;

    public CourseDataPoint() {}

    public CourseDataPoint(String courseName, double avgScore) {
        this.courseName = courseName;
        this.avgScore = avgScore;
    }

    // Getters and Setters
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
    
    // Alias for chart compatibility - the JavaScript expects passRate
    public double getPassRate() { return avgScore; }
}