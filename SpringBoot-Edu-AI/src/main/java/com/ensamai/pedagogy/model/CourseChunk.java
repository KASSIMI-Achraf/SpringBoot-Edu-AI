package com.ensamai.pedagogy.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class CourseChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Double> embedding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    public CourseChunk() {
    }

    // --- MANUALLY ADDED GETTERS AND SETTERS ---
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<Double> getEmbedding() { return embedding; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}