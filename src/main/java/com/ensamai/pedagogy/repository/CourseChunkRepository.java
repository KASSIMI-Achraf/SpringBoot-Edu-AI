package com.ensamai.pedagogy.repository;

import com.ensamai.pedagogy.model.CourseChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseChunkRepository extends JpaRepository<CourseChunk, Long> {
    // Fetch all chunks for a specific course to perform similarity search
    List<CourseChunk> findByCourseId(Long courseId);
}