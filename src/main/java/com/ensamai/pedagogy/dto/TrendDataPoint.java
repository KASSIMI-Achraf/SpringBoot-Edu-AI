package com.ensamai.pedagogy.dto;

public class TrendDataPoint {
    private String date;
    private double avgScore;
    private int quizCount;

    public TrendDataPoint() {}

    public TrendDataPoint(String date, double avgScore, int quizCount) {
        this.date = date;
        this.avgScore = avgScore;
        this.quizCount = quizCount;
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
    
    public int getQuizCount() { return quizCount; }
    public void setQuizCount(int quizCount) { this.quizCount = quizCount; }
}