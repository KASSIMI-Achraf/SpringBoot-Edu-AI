package com.ensamai.pedagogy.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    // --- NEW PDF FIELDS ---
    @Lob // Tells DB this is a large object
    @Column(length = 10000000) // Increase size limit for MySQL/H2
    private byte[] pdfFile;

    private String pdfFilename;
    // ----------------------

    @ManyToMany
    @JoinTable(
        name = "course_students",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<AppUser> students = new ArrayList<>();

    // --- GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public byte[] getPdfFile() { return pdfFile; }
    public void setPdfFile(byte[] pdfFile) { this.pdfFile = pdfFile; }

    public String getPdfFilename() { return pdfFilename; }
    public void setPdfFilename(String pdfFilename) { this.pdfFilename = pdfFilename; }

    public List<AppUser> getStudents() { return students; }
    public void setStudents(List<AppUser> students) { this.students = students; }

    public void enrollStudent(AppUser student) {
        if (!students.contains(student)) {
            students.add(student);
        }
    }
}