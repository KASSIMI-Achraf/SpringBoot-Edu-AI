package com.ensamai.pedagogy.dto;

public class PerformanceDataPoint {
    private String date;
    private double score;

    public PerformanceDataPoint() {}

    public PerformanceDataPoint(String date, double score) {
        this.date = date;
        this.score = score;
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}