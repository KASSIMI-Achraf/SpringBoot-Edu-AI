package com.ensamai.pedagogy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score; 
    
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private AppUser student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    @Column(name = "total_questions")
    private Integer totalQuestions;
    private Integer timeTaken; // in minutes

    public QuizResult() {
        this.completedAt = LocalDateTime.now();
    }

    public QuizResult(AppUser student, Course course, int score) {
        this.student = student;
        this.course = course;
        this.score = score;
        this.completedAt = LocalDateTime.now();
    }

    public QuizResult(AppUser student, Course course, int score, Integer correctAnswers, Integer totalQuestions) {
        this.student = student;
        this.course = course;
        this.score = score;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.completedAt = LocalDateTime.now();
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getScorePercentage() {
        return this.score; 
    }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public AppUser getStudent() { return student; }
    public void setStudent(AppUser student) { this.student = student; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getTimeTaken() { return timeTaken; }
    public void setTimeTaken(Integer timeTaken) { this.timeTaken = timeTaken; }
}