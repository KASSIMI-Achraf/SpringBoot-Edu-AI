package com.ensamai.pedagogy.controller;

import com.ensamai.pedagogy.model.Course;
import com.ensamai.pedagogy.repository.CourseRepository;
import com.ensamai.pedagogy.service.AiAgentService;
import com.ensamai.pedagogy.service.RagService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class CourseController {

    private final CourseRepository courseRepository;
    private final RagService ragService;       // Added for Ingestion
    private final AiAgentService aiAgentService; // Added for Quiz Generation

    // Constructor Injection
    public CourseController(CourseRepository courseRepository, 
                            RagService ragService, 
                            AiAgentService aiAgentService) {
        this.courseRepository = courseRepository;
        this.ragService = ragService;
        this.aiAgentService = aiAgentService;
    }
    
    // --- 1. VIEW: List Courses (Existing) ---
    @GetMapping("/courses")
    public String listCourses(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "student/courses"; 
    }

    // --- 2. ADMIN: Create Course + RAG INGESTION (New) ---
    // Assuming you have a form at /course/create that posts here
    @PostMapping("/course/save")
    public String saveCourse(@ModelAttribute Course course, 
                             @RequestParam("file") MultipartFile file) throws IOException {
        
        // A. Handle PDF Upload
        if (!file.isEmpty()) {
            course.setPdfFile(file.getBytes());
            course.setPdfFilename(file.getOriginalFilename());
        }

        // B. Save to Database
        Course savedCourse = courseRepository.save(course);

        // C. TRIGGER RAG: Chunk and Embed the text content
        // This ensures the AI "reads" the course immediately.
        if (savedCourse.getContent() != null && !savedCourse.getContent().isEmpty()) {
            ragService.ingestCourse(savedCourse);
        }

        return "redirect:/courses";
    }

    // --- 3. STUDENT: Download PDF (Existing) ---
    @GetMapping("/course/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadPdf(@PathVariable Long id) {
        Course course = courseRepository.findById(id).orElseThrow();

        if (course.getPdfFile() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + course.getPdfFilename() + "\"")
                .body(new ByteArrayResource(course.getPdfFile()));
    }

    // --- 4. STUDENT: Generate Quiz via AI AGENT (New) ---
    // This endpoint returns JSON data for your JavaScript frontend to render
    @GetMapping("/course/{id}/quiz-data")
    @ResponseBody // Important: Tells Spring to return data, not a view
    public String generateQuiz(@PathVariable Long id, @RequestParam Long studentId) {
        // The Agent checks history -> Retrieves RAG context -> Generates Questions
        return aiAgentService.generateQuiz(studentId, id);
    }
}