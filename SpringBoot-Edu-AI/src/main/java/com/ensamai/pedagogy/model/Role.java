package com.ensamai.pedagogy.model;

public enum Role {
    ADMIN,    // Super admin (single account only) - full system access
    TEACHER,  // Manages courses and students
    STUDENT   // End users - can take quizzes
}
