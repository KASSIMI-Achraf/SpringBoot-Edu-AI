package com.ensamai.pedagogy.dto;

public class CoursePerformanceDTO {
    private Long id;
    private String name;
    private int attempts;
    private double avgScore;

    public CoursePerformanceDTO() {}

    public CoursePerformanceDTO(Long id, String name, int attempts, double avgScore) {
        this.id = id;
        this.name = name;
        this.attempts = attempts;
        this.avgScore = avgScore;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    
    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
}
