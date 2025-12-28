package com.ensamai.pedagogy.dto;

public class CourseStatDTO {
    private Long courseId;
    private String courseName;
    private int studentsEnrolled;
    private int quizzesTaken;
    private double avgScore;
    private double passRate;

    public CourseStatDTO() {}

    public CourseStatDTO(Long courseId, String courseName, int studentsEnrolled, int quizzesTaken, double avgScore, double passRate) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.studentsEnrolled = studentsEnrolled;
        this.quizzesTaken = quizzesTaken;
        this.avgScore = avgScore;
        this.passRate = passRate;
    }

    // Getters and Setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public int getStudentsEnrolled() { return studentsEnrolled; }
    public void setStudentsEnrolled(int studentsEnrolled) { this.studentsEnrolled = studentsEnrolled; }
    
    public int getQuizzesTaken() { return quizzesTaken; }
    public void setQuizzesTaken(int quizzesTaken) { this.quizzesTaken = quizzesTaken; }
    
    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
    
    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
}