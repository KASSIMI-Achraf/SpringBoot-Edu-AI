package com.ensamai.pedagogy.dto;

public class StudentRankingDTO {
    private Long id;
    private String username;
    private int quizzesTaken;
    private double avgScore;
    private int failureCount;

    public StudentRankingDTO() {}

    public StudentRankingDTO(Long id, String username, int quizzesTaken, double avgScore, int failureCount) {
        this.id = id;
        this.username = username;
        this.quizzesTaken = quizzesTaken;
        this.avgScore = avgScore;
        this.failureCount = failureCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getQuizzesTaken() { return quizzesTaken; }
    public void setQuizzesTaken(int quizzesTaken) { this.quizzesTaken = quizzesTaken; }
    
    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
    
    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
}