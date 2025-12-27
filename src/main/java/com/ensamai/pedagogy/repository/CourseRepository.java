package com.ensamai.pedagogy.repository;

import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // Find courses by teacher
    List<Course> findByTeacher(AppUser teacher);
    
    // Find courses by teacher ID
    List<Course> findByTeacherId(Long teacherId);
    
    // Count courses by teacher
    long countByTeacherId(Long teacherId);
}