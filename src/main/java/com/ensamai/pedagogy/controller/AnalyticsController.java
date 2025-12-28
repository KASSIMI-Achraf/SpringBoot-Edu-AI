package com.ensamai.pedagogy.controller;

import com.ensamai.pedagogy.dto.*;
import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.QuizResult;
import com.ensamai.pedagogy.repository.AppUserRepository;
import com.ensamai.pedagogy.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private AppUserRepository appUserRepository;

    // STUDENT ANALYTICS
    
    @GetMapping("/student/analytics")
    @PreAuthorize("hasAuthority('STUDENT')")
    public String studentAnalytics(Authentication auth, Model model) {
        Long studentId = getCurrentStudentId(auth);
        
        // Get analytics data
        StudentAnalyticsDTO analytics = analyticsService.getStudentAnalytics(studentId);
        List<PerformanceDataPoint> performanceData = analyticsService.getStudentPerformanceTrend(studentId);
        List<CourseDataPoint> courseData = analyticsService.getStudentCourseBreakdown(studentId);
        List<CoursePerformanceDTO> strongCourses = analyticsService.getStrongCourses(studentId);
        List<CoursePerformanceDTO> weakCourses = analyticsService.getWeakCourses(studentId);
        List<QuizResult> recentResults = analyticsService.getRecentResults(studentId, 10);
        
        // Add to model
        model.addAttribute("totalQuizzes", analytics.getTotalQuizzes());
        model.addAttribute("averageScore", analytics.getAverageScore());
        model.addAttribute("passRate", analytics.getPassRate());
        model.addAttribute("coursesEnrolled", analytics.getCoursesEnrolled());
        model.addAttribute("performanceData", performanceData);
        model.addAttribute("courseData", courseData);
        model.addAttribute("strongCourses", strongCourses);
        model.addAttribute("weakCourses", weakCourses);
        model.addAttribute("recentResults", recentResults);
        
        return "student/analytics";
    }

    // TEACHER/ADMIN ANALYTICS
  
    @GetMapping("/teacher/analytics")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    public String teacherAnalytics(
            @RequestParam(defaultValue = "30") int days,
            Model model) {
        
        // Get analytics data
        AdminAnalyticsDTO analytics = analyticsService.getAdminAnalytics();
        List<TrendDataPoint> trendData = analyticsService.getClassTrend(days);
        List<StudentRankingDTO> topPerformers = analyticsService.getTopPerformers(5);
        List<StudentRankingDTO> strugglingStudents = analyticsService.getStrugglingStudents(5);
        List<CourseStatDTO> courseStats = analyticsService.getCourseStats();
        List<CourseDataPoint> coursePassRateData = analyticsService.getCoursePassRates();
        List<RecentActivityDTO> recentActivity = analyticsService.getRecentActivity(10);
        
        // Add to model
        model.addAttribute("totalStudents", analytics.getTotalStudents());
        model.addAttribute("totalQuizzesTaken", analytics.getTotalQuizzesTaken());
        model.addAttribute("classAverageScore", analytics.getClassAverageScore());
        model.addAttribute("overallPassRate", analytics.getOverallPassRate());
        model.addAttribute("trendData", trendData);
        model.addAttribute("topPerformers", topPerformers);
        model.addAttribute("strugglingStudents", strugglingStudents);
        model.addAttribute("courseStats", courseStats);
        model.addAttribute("coursePassRateData", coursePassRateData);
        model.addAttribute("recentActivity", recentActivity);
        
        return "teacher/analytics";
    }
    
    private Long getCurrentStudentId(Authentication auth) {
        String username = auth.getName();
        AppUser user = appUserRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}