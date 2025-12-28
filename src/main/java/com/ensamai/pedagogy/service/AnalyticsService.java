package com.ensamai.pedagogy.service;

import com.ensamai.pedagogy.dto.*;
import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Course;
import com.ensamai.pedagogy.model.QuizResult;
import com.ensamai.pedagogy.model.Role;
import com.ensamai.pedagogy.repository.AppUserRepository;
import com.ensamai.pedagogy.repository.CourseRepository;
import com.ensamai.pedagogy.repository.QuizResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    
    @Autowired
    private QuizResultRepository quizResultRepository;
    
    @Autowired
    private AppUserRepository appUserRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    // STUDENT ANALYTICS
    public StudentAnalyticsDTO getStudentAnalytics(Long studentId) {
        List<QuizResult> results = quizResultRepository.findByStudentId(studentId);
        
        int totalQuizzes = results.size();
        double averageScore = results.isEmpty() ? 0 : 
            results.stream().mapToDouble(QuizResult::getScore).average().orElse(0);
        
        long passedCount = results.stream().filter(r -> r.getScore() >= 50).count();
        double passRate = totalQuizzes == 0 ? 0 : (passedCount * 100.0 / totalQuizzes);
        
        int coursesEnrolled = (int) results.stream()
            .map(r -> r.getCourse().getId())
            .distinct()
            .count();
        
        return new StudentAnalyticsDTO(
            totalQuizzes,
            Math.round(averageScore * 100.0) / 100.0,
            Math.round(passRate * 100.0) / 100.0,
            coursesEnrolled
        );
    }
    
    public List<PerformanceDataPoint> getStudentPerformanceTrend(Long studentId) {
        List<QuizResult> results = quizResultRepository.findByStudentIdOrderByCompletedAtAsc(studentId);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        
        return results.stream()
            .map(r -> new PerformanceDataPoint(
                r.getCompletedAt().format(formatter),
                r.getScore()
            ))
            .collect(Collectors.toList());
    }
    
    public List<CourseDataPoint> getStudentCourseBreakdown(Long studentId) {
        List<QuizResult> results = quizResultRepository.findByStudentId(studentId);
        
        Map<String, List<QuizResult>> groupedByCourse = results.stream()
            .collect(Collectors.groupingBy(r -> r.getCourse().getTitle()));
        
        return groupedByCourse.entrySet().stream()
            .map(entry -> {
                double avgScore = entry.getValue().stream()
                    .mapToDouble(QuizResult::getScore)
                    .average()
                    .orElse(0);
                return new CourseDataPoint(entry.getKey(), Math.round(avgScore * 100.0) / 100.0);
            })
            .collect(Collectors.toList());
    }
    
    public List<CoursePerformanceDTO> getStrongCourses(Long studentId) {
        return getCoursePerformance(studentId, true);
    }
    
    public List<CoursePerformanceDTO> getWeakCourses(Long studentId) {
        return getCoursePerformance(studentId, false);
    }
    
    private List<CoursePerformanceDTO> getCoursePerformance(Long studentId, boolean strong) {
        List<QuizResult> results = quizResultRepository.findByStudentId(studentId);
        
        Map<Long, List<QuizResult>> groupedByCourse = results.stream()
            .collect(Collectors.groupingBy(r -> r.getCourse().getId()));
        
        return groupedByCourse.entrySet().stream()
            .map(entry -> {
                List<QuizResult> courseResults = entry.getValue();
                double avgScore = courseResults.stream()
                    .mapToDouble(QuizResult::getScore)
                    .average()
                    .orElse(0);
                
                return new CoursePerformanceDTO(
                    entry.getKey(),
                    courseResults.get(0).getCourse().getTitle(),
                    courseResults.size(),
                    Math.round(avgScore * 100.0) / 100.0
                );
            })
            .filter(dto -> strong ? dto.getAvgScore() >= 80 : dto.getAvgScore() < 50)
            .sorted((a, b) -> strong ? 
                Double.compare(b.getAvgScore(), a.getAvgScore()) : 
                Double.compare(a.getAvgScore(), b.getAvgScore()))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    public List<QuizResult> getRecentResults(Long studentId, int limit) {
        return quizResultRepository.findByStudentIdOrderByCompletedAtDesc(studentId)
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    // ADMIN ANALYTICs
    public AdminAnalyticsDTO getAdminAnalytics() {
        int totalStudents = (int) appUserRepository.countByRole(Role.STUDENT);
        int totalQuizzesTaken = (int) quizResultRepository.count();
        
        double classAverageScore = quizResultRepository.findAll().stream()
            .mapToDouble(QuizResult::getScore)
            .average()
            .orElse(0);
        
        long passedCount = quizResultRepository.findAll().stream()
            .filter(r -> r.getScore() >= 50)
            .count();
        
        double overallPassRate = totalQuizzesTaken == 0 ? 0 : 
            (passedCount * 100.0 / totalQuizzesTaken);
        
        return new AdminAnalyticsDTO(
            totalStudents,
            totalQuizzesTaken,
            Math.round(classAverageScore * 100.0) / 100.0,
            Math.round(overallPassRate * 100.0) / 100.0
        );
    }
    
    public List<TrendDataPoint> getClassTrend(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<QuizResult> results = quizResultRepository.findByCompletedAtAfter(startDate);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        
        Map<String, List<QuizResult>> groupedByDate = results.stream()
            .collect(Collectors.groupingBy(
                r -> r.getCompletedAt().toLocalDate().format(formatter)
            ));
        
        return groupedByDate.entrySet().stream()
            .map(entry -> {
                List<QuizResult> dayResults = entry.getValue();
                double avgScore = dayResults.stream()
                    .mapToDouble(QuizResult::getScore)
                    .average()
                    .orElse(0);
                
                return new TrendDataPoint(
                    entry.getKey(),
                    Math.round(avgScore * 100.0) / 100.0,
                    dayResults.size()
                );
            })
            .sorted(Comparator.comparing(TrendDataPoint::getDate)) // Note: This might need a date parser if format is "MMM dd"
            .collect(Collectors.toList());
    }
    
    public List<StudentRankingDTO> getTopPerformers(int limit) {
        return appUserRepository.findByRole(Role.STUDENT).stream()
            .map(student -> {
                List<QuizResult> results = quizResultRepository.findByStudentId(student.getId());
                if (results.isEmpty()) return null;
                
                double avgScore = results.stream()
                    .mapToDouble(QuizResult::getScore)
                    .average()
                    .orElse(0);
                
                return new StudentRankingDTO(
                    student.getId(),
                    student.getUsername(),
                    results.size(),
                    Math.round(avgScore * 100.0) / 100.0,
                    0
                );
            })
            .filter(Objects::nonNull)
            .sorted((a, b) -> Double.compare(b.getAvgScore(), a.getAvgScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<StudentRankingDTO> getStrugglingStudents(int limit) {
        return appUserRepository.findByRole(Role.STUDENT).stream()
            .map(student -> {
                List<QuizResult> results = quizResultRepository.findByStudentId(student.getId());
                if (results.isEmpty()) return null;
                
                double avgScore = results.stream()
                    .mapToDouble(QuizResult::getScore)
                    .average()
                    .orElse(0);
                
                long failureCount = results.stream()
                    .filter(r -> r.getScore() < 50)
                    .count();
                
                if (avgScore >= 50) return null;
                
                return new StudentRankingDTO(
                    student.getId(),
                    student.getUsername(),
                    results.size(),
                    Math.round(avgScore * 100.0) / 100.0,
                    (int) failureCount
                );
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(StudentRankingDTO::getAvgScore))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<CourseStatDTO> getCourseStats() {
        return courseRepository.findAll().stream()
            .map(course -> {
                List<QuizResult> results = quizResultRepository.findByCourseId(course.getId());
                
                int studentsEnrolled = course.getStudents().size();
                int quizzesTaken = results.size();
                
                double avgScore = results.isEmpty() ? 0 : results.stream()
                    .mapToDouble(QuizResult::getScore)
                    .average()
                    .orElse(0);
                
                long passedCount = results.stream()
                    .filter(r -> r.getScore() >= 50)
                    .count();
                
                double passRate = quizzesTaken == 0 ? 0 : (passedCount * 100.0 / quizzesTaken);
                
                return new CourseStatDTO(
                    course.getId(),
                    course.getTitle(),
                    studentsEnrolled,
                    quizzesTaken,
                    Math.round(avgScore * 100.0) / 100.0,
                    Math.round(passRate * 100.0) / 100.0
                );
            })
            .collect(Collectors.toList());
    }
    
    public List<CourseDataPoint> getCoursePassRates() {
        return courseRepository.findAll().stream()
            .map(course -> {
                List<QuizResult> results = quizResultRepository.findByCourseId(course.getId());
                
                long passedCount = results.stream()
                    .filter(r -> r.getScore() >= 50)
                    .count();
                
                double passRate = results.isEmpty() ? 0 : (passedCount * 100.0 / results.size());
                
                return new CourseDataPoint(
                    course.getTitle(),
                    Math.round(passRate * 100.0) / 100.0
                );
            })
            .collect(Collectors.toList());
    }
    
    public List<RecentActivityDTO> getRecentActivity(int limit) {
        return quizResultRepository.findAllByOrderByCompletedAtDesc()
            .stream()
            .limit(limit)
            .map(result -> new RecentActivityDTO(
                result.getStudent().getUsername(),
                result.getCourse().getTitle(),
                result.getScore(),
                result.getScore() >= 50,
                result.getCompletedAt()
            ))
            .collect(Collectors.toList());
    }
}