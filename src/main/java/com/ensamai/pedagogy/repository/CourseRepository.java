package com.ensamai.pedagogy.repository;

import com.ensamai.pedagogy.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // We can add custom search methods later if needed
}