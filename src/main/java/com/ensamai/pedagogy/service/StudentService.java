package com.ensamai.pedagogy.service;

import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Course;
import com.ensamai.pedagogy.model.QuizResult;
import com.ensamai.pedagogy.model.Role;
import com.ensamai.pedagogy.repository.AppUserRepository;
import com.ensamai.pedagogy.repository.CourseRepository;
import com.ensamai.pedagogy.repository.QuizResultRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentService {

    private final AppUserRepository appUserRepository;
    private final QuizResultRepository quizResultRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(AppUserRepository appUserRepository, QuizResultRepository quizResultRepository, 
                          CourseRepository courseRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.quizResultRepository = quizResultRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> getAllStudents() {
        return appUserRepository.findByRole(Role.STUDENT);
    }

    public AppUser getStudentById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
    }

    @Transactional
    public AppUser createStudent(AppUser student) {
        student.setRole(Role.STUDENT);
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        student.setActive(true);
        student.setDeletable(true);
        student.setCreatedAt(LocalDateTime.now());
        return appUserRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        AppUser student = getStudentById(id);

        // Delete quiz results
        List<QuizResult> results = quizResultRepository.findByStudent(student);
        quizResultRepository.deleteAll(results);

        // Remove from course enrollments
        List<Course> allCourses = courseRepository.findAll();
        for (Course c : allCourses) {
            if (c.getStudents().contains(student)) {
                c.getStudents().remove(student);
                courseRepository.save(c);
            }
        }

        appUserRepository.delete(student);
    }

    public List<QuizResult> getStudentProgress(Long id) {
        AppUser student = getStudentById(id);
        return quizResultRepository.findByStudent(student);
    }
}
