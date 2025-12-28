package com.ensamai.pedagogy.repository;

import com.ensamai.pedagogy.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    // Fetch history for a specific student on a specific course
    // Ordered by ID/Date so the last one is the most recent
    @Query("SELECT r FROM QuizResult r WHERE r.student.id = :studentId AND r.course.id = :courseId ORDER BY r.completedAt ASC")
    List<QuizResult> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    // Existing method used in Admin Controller
    List<QuizResult> findByStudent(com.ensamai.pedagogy.model.AppUser student);
    List<QuizResult> findByStudentId(Long studentId);
    List<QuizResult> findByStudentIdOrderByCompletedAtAsc(Long studentId);
    List<QuizResult> findByStudentIdOrderByCompletedAtDesc(Long studentId);
    List<QuizResult> findByCourseId(Long courseId);
    List<QuizResult> findByCompletedAtAfter(LocalDateTime date);
    List<QuizResult> findAllByOrderByCompletedAtDesc();
}