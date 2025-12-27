package com.ensamai.pedagogy.controller;

import com.ensamai.pedagogy.model.AppUser;
import com.ensamai.pedagogy.model.Course;
import com.ensamai.pedagogy.model.Role;
import com.ensamai.pedagogy.repository.AppUserRepository;
import com.ensamai.pedagogy.repository.CourseRepository;
import com.ensamai.pedagogy.service.AiAgentService;
import com.ensamai.pedagogy.service.RagService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class CourseController {

    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;
    private final RagService ragService;
    private final AiAgentService aiAgentService;

    public CourseController(CourseRepository courseRepository,
                            AppUserRepository appUserRepository,
                            RagService ragService, 
                            AiAgentService aiAgentService) {
        this.courseRepository = courseRepository;
        this.appUserRepository = appUserRepository;
        this.ragService = ragService;
        this.aiAgentService = aiAgentService;
    }

    /**
     * Get current user from authentication
     */
    private AppUser getCurrentUser(Authentication auth) {
        return appUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    // --- 1. VIEW: List Courses for Student ---
    @GetMapping("/courses")
    public String listCourses(Model model, Authentication auth) {
        AppUser user = getCurrentUser(auth);
        
        List<Course> courses;
        if (user.getRole() == Role.STUDENT) {
            // Students only see courses they're enrolled in
            courses = user.getCourses();
        } else {
            // Teachers/Admin see all courses
            courses = courseRepository.findAll();
        }
        
        model.addAttribute("courses", courses);
        return "student/courses"; 
    }

    // --- 2. VIEW: Course Detail Page (Material & PDF) ---
    @GetMapping("/course/{id}")
    public String viewCourseDetail(@PathVariable Long id, Model model, Authentication auth) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        AppUser user = getCurrentUser(auth);
        
        // Security check: Students can only view courses they're enrolled in
        if (user.getRole() == Role.STUDENT && !user.getCourses().contains(course)) {
            return "redirect:/courses?error=not_enrolled";
        }
        
        model.addAttribute("course", course);
        model.addAttribute("hasPdf", course.getPdfFile() != null && course.getPdfFile().length > 0);
        
        return "student/course_detail";
    }

    // --- 3. ADMIN: Create Course + RAG INGESTION ---
    @PostMapping("/course/save")
    public String saveCourse(@ModelAttribute Course course, 
                             @RequestParam("file") MultipartFile file) throws IOException {
        
        if (!file.isEmpty()) {
            course.setPdfFile(file.getBytes());
            course.setPdfFilename(file.getOriginalFilename());
        }

        Course savedCourse = courseRepository.save(course);

        if (savedCourse.getContent() != null && !savedCourse.getContent().isEmpty()) {
            ragService.ingestCourse(savedCourse);
        }

        return "redirect:/courses";
    }

    // --- 4. STUDENT: Download PDF ---
    @GetMapping("/course/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadPdf(@PathVariable Long id, Authentication auth) {
        Course course = courseRepository.findById(id).orElseThrow();
        AppUser user = getCurrentUser(auth);
        
        // Security check: Students can only download from enrolled courses
        if (user.getRole() == Role.STUDENT && !user.getCourses().contains(course)) {
            return ResponseEntity.status(403).build();
        }

        if (course.getPdfFile() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + course.getPdfFilename() + "\"")
                .body(new ByteArrayResource(course.getPdfFile()));
    }

    // --- 5. STUDENT: View PDF inline (opens in browser/iframe) ---
    @GetMapping("/course/{id}/pdf")
    public ResponseEntity<ByteArrayResource> viewPdf(@PathVariable Long id, Authentication auth) {
        Course course = courseRepository.findById(id).orElseThrow();
        AppUser user = getCurrentUser(auth);
        
        // Security check: Students can only view enrolled courses
        // Teachers and admins can view all
        if (user.getRole() == Role.STUDENT && !user.getCourses().contains(course)) {
            return ResponseEntity.status(403).build();
        }

        if (course.getPdfFile() == null || course.getPdfFile().length == 0) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfData = course.getPdfFile();
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + course.getPdfFilename() + "\"")
                .header("X-Frame-Options", "SAMEORIGIN")
                .body(new ByteArrayResource(pdfData));
    }

    // --- 6. STUDENT: Generate Quiz via AI AGENT ---
    @GetMapping("/course/{id}/quiz-data")
    @ResponseBody
    public String generateQuiz(@PathVariable Long id, @RequestParam Long studentId) {
        return aiAgentService.generateQuiz(studentId, id);
    }
}