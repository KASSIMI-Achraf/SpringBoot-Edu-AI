package com.ensamai.pedagogy.repository;

import com.ensamai.pedagogy.model.CourseChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseChunkRepository extends JpaRepository<CourseChunk, Long> {

    List<CourseChunk> findByCourseId(Long courseId);
}