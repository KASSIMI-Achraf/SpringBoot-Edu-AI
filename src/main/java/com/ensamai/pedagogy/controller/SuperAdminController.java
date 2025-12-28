package com.ensamai.pedagogy.controller;

import com.ensamai.pedagogy.dto.SystemAnalyticsDTO;
import com.ensamai.pedagogy.dto.TeacherDTO;
import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Course;
import com.ensamai.pedagogy.model.QuizResult;
import com.ensamai.pedagogy.model.Role;
import com.ensamai.pedagogy.repository.AppUserRepository;
import com.ensamai.pedagogy.repository.CourseRepository;
import com.ensamai.pedagogy.repository.QuizResultRepository;
import com.ensamai.pedagogy.service.TeacherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for admin-only functionality (teacher management, system overview)
 * Only ADMIN role can access these endpoints
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class SuperAdminController {

    private final TeacherService teacherService;
    private final com.ensamai.pedagogy.service.StudentService studentService;
    private final AppUserRepository appUserRepository;
    private final CourseRepository courseRepository;
    private final QuizResultRepository quizResultRepository;
    private final com.ensamai.pedagogy.service.AnalyticsService analyticsService;

    public SuperAdminController(TeacherService teacherService, 
                                com.ensamai.pedagogy.service.StudentService studentService,
                                AppUserRepository appUserRepository,
                                CourseRepository courseRepository, QuizResultRepository quizResultRepository,
                                com.ensamai.pedagogy.service.AnalyticsService analyticsService) {
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.appUserRepository = appUserRepository;
        this.courseRepository = courseRepository;
        this.quizResultRepository = quizResultRepository;
        this.analyticsService = analyticsService;
    }

    /**
     * Get current admin from authentication
     */
    private AppUser getCurrentAdmin(Authentication auth) {
        return appUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // System-wide analytics
        int totalTeachers = (int) appUserRepository.countByRole(Role.TEACHER);
        int totalStudents = (int) appUserRepository.countByRole(Role.STUDENT);
        int totalCourses = (int) courseRepository.count();
        int totalQuizzes = (int) quizResultRepository.count();
        
        double avgScore = quizResultRepository.findAll().stream()
                .mapToDouble(QuizResult::getScore)
                .average()
                .orElse(0);
        
        long passedCount = quizResultRepository.findAll().stream()
                .filter(r -> r.getScore() >= 50)
                .count();
        double passRate = totalQuizzes == 0 ? 0 : (passedCount * 100.0 / totalQuizzes);

        SystemAnalyticsDTO analytics = new SystemAnalyticsDTO(
            totalTeachers, totalStudents, totalCourses, totalQuizzes,
            Math.round(avgScore * 100.0) / 100.0,
            Math.round(passRate * 100.0) / 100.0
        );

        // Recent teachers
        List<AppUser> recentTeachers = appUserRepository.findByRole(Role.TEACHER);

        model.addAttribute("analytics", analytics);
        model.addAttribute("teachers", recentTeachers);
        model.addAttribute("allCourses", courseRepository.findAll());
        
        return "admin/admin_dashboard";
    }

    // TEACHER MANAGEMENT

    @GetMapping("/teachers")
    public String listTeachers(Model model) {
        List<AppUser> teachers = teacherService.getAllTeachers();
        model.addAttribute("teachers", teachers);
        return "admin/admin_teachers";
    }

    @GetMapping("/teachers/create")
    public String showCreateTeacherForm(Model model) {
        model.addAttribute("teacher", new TeacherDTO());
        model.addAttribute("isEdit", false);
        return "admin/admin_teacher_form";
    }

    @PostMapping("/teachers")
    public String createTeacher(@ModelAttribute TeacherDTO teacherDTO, 
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            AppUser admin = getCurrentAdmin(auth);
            teacherService.createTeacher(teacherDTO, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/teachers/create";
        }
        return "redirect:/admin/teachers";
    }

    @GetMapping("/teachers/{id}")
    public String viewTeacher(@PathVariable Long id, Model model) {
        AppUser teacher = teacherService.getTeacherById(id);
        List<Course> teacherCourses = courseRepository.findByTeacherId(id);
        
        model.addAttribute("teacher", teacher);
        model.addAttribute("courses", teacherCourses);
        return "admin/admin_teacher_details";
    }

    @GetMapping("/teachers/{id}/edit")
    public String showEditTeacherForm(@PathVariable Long id, Model model) {
        AppUser teacher = teacherService.getTeacherById(id);
        
        TeacherDTO dto = new TeacherDTO();
        dto.setId(teacher.getId());
        dto.setUsername(teacher.getUsername());
        dto.setEmail(teacher.getEmail());
        dto.setFullName(teacher.getFullName());
        dto.setActive(teacher.isActive());
        
        model.addAttribute("teacher", dto);
        model.addAttribute("isEdit", true);
        return "admin/admin_teacher_form";
    }

    @PostMapping("/teachers/{id}")
    public String updateTeacher(@PathVariable Long id, @ModelAttribute TeacherDTO teacherDTO,
                               RedirectAttributes redirectAttributes) {
        try {
            teacherService.updateTeacher(id, teacherDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher updated successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/teachers/" + id + "/edit";
        }
        return "redirect:/admin/teachers";
    }

    @PostMapping("/teachers/{id}/activate")
    public String activateTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.activateTeacher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher activated successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/teachers";
    }

    @PostMapping("/teachers/{id}/deactivate")
    public String deactivateTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deactivateTeacher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher deactivated successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/teachers";
    }

    @PostMapping("/teachers/{id}/delete")
    public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteTeacher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher deleted successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/teachers";
    }

    @GetMapping("/teachers/{id}/courses")
    public String viewTeacherCourses(@PathVariable Long id, Model model) {
        AppUser teacher = teacherService.getTeacherById(id);
        List<Course> courses = courseRepository.findByTeacherId(id);
        
        model.addAttribute("teacher", teacher);
        model.addAttribute("courses", courses);
        return "admin/admin_teacher_courses";
    }

    @GetMapping("/courses")
    public String viewAllCourses(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/admin_courses";
    }

    @GetMapping("/analytics")
    public String viewSystemAnalytics(@RequestParam(defaultValue = "30") int days, Model model) {
        // Reuse analytics logic but for system-wide view
        com.ensamai.pedagogy.dto.AdminAnalyticsDTO analytics = analyticsService.getAdminAnalytics();
        List<com.ensamai.pedagogy.dto.TrendDataPoint> trendData = analyticsService.getClassTrend(days);
        List<com.ensamai.pedagogy.dto.CourseStatDTO> courseStats = analyticsService.getCourseStats();
        List<com.ensamai.pedagogy.dto.CourseDataPoint> coursePassRateData = analyticsService.getCoursePassRates();
        
        model.addAttribute("totalStudents", analytics.getTotalStudents());
        model.addAttribute("totalQuizzesTaken", analytics.getTotalQuizzesTaken());
        model.addAttribute("classAverageScore", analytics.getClassAverageScore());
        model.addAttribute("overallPassRate", analytics.getOverallPassRate());
        model.addAttribute("trendData", trendData);
        model.addAttribute("courseStats", courseStats);
        model.addAttribute("coursePassRateData", coursePassRateData);
        
        return "admin/admin_analytics";
    }

    // STUDENT MANAGEMENT

    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "admin/student_list";
    }

    @GetMapping("/create-student")
    public String showCreateStudentForm(Model model) {
        model.addAttribute("student", new AppUser());
        return "admin/student_create";
    }

    @PostMapping("/create-student")
    public String createStudent(@ModelAttribute AppUser student, RedirectAttributes redirectAttributes) {
        try {
            studentService.createStudent(student);
            redirectAttributes.addFlashAttribute("successMessage", "Student registered successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating student: " + e.getMessage());
            return "redirect:/admin/create-student";
        }
        return "redirect:/admin/students";
    }

    @GetMapping("/student/{id}/progress")
    public String viewStudentProgress(@PathVariable Long id, Model model) {
        AppUser student = studentService.getStudentById(id);
        List<QuizResult> results = studentService.getStudentProgress(id);
        model.addAttribute("student", student);
        model.addAttribute("results", results);
        return "admin/student_details";
    }

    @PostMapping("/student/{id}/delete")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting student: " + e.getMessage());
        }
        return "redirect:/admin/students";
    }
}
