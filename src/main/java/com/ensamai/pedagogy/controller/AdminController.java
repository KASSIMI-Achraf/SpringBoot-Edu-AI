package com.ensamai.pedagogy.controller;

import com.ensamai.pedagogy.model.*;
import com.ensamai.pedagogy.repository.*;
import com.ensamai.pedagogy.service.GeminiService;
import com.ensamai.pedagogy.service.RagService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuestionRepository questionRepository;
    private final CourseChunkRepository courseChunkRepository; // Added this
    private final GeminiService geminiService;
    private final RagService ragService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // Updated Constructor with CourseChunkRepository
    public AdminController(CourseRepository courseRepository, AppUserRepository appUserRepository,
                           QuizResultRepository quizResultRepository, QuestionRepository questionRepository,
                           CourseChunkRepository courseChunkRepository, // Injected here
                           GeminiService geminiService, RagService ragService, 
                           PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.courseRepository = courseRepository;
        this.appUserRepository = appUserRepository;
        this.quizResultRepository = quizResultRepository;
        this.questionRepository = questionRepository;
        this.courseChunkRepository = courseChunkRepository; // Assigned here
        this.geminiService = geminiService;
        this.ragService = ragService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    // --- DASHBOARD ---
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/dashboard";
    }

    // --- ENROLLMENT ---
    @GetMapping("/enroll/{id}")
    public String showEnrollForm(@PathVariable Long id, Model model) {
        Course course = courseRepository.findById(id).orElseThrow();
        List<AppUser> students = appUserRepository.findByRole(Role.STUDENT);
        model.addAttribute("course", course);
        model.addAttribute("students", students);
        return "admin/enroll";
    }

    @PostMapping("/enroll")
    public String enrollStudent(@RequestParam Long courseId, @RequestParam Long studentId) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        AppUser student = appUserRepository.findById(studentId).orElseThrow();
        course.enrollStudent(student);
        courseRepository.save(course);
        return "redirect:/admin/dashboard";
    }

    // --- STUDENTS ---
    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", appUserRepository.findByRole(Role.STUDENT));
        return "admin/student_list"; 
    }

    @GetMapping("/create-student")
    public String showCreateStudentForm(Model model) {
        model.addAttribute("student", new AppUser());
        return "admin/student_create";
    }

    @PostMapping("/create-student")
    public String createStudent(@ModelAttribute AppUser student) {
        student.setRole(Role.STUDENT);
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        appUserRepository.save(student);
        return "redirect:/admin/students";
    }

    // --- QUIZ & AI ---
    @GetMapping("/course/{id}/generate-quiz")
    public String generateQuizPreview(@PathVariable Long id, Model model) {
        Course course = courseRepository.findById(id).orElseThrow();
        String prompt = "Based on the text below, generate 5 multiple choice questions. " +
                        "Output strictly as a JSON array of objects with keys: " +
                        "questionText, optionA, optionB, optionC, optionD, correctAnswer (A/B/C/D). " +
                        "Text: " + course.getContent();

        String jsonResponse = geminiService.generateContent(prompt);
        List<Question> aiQuestions = new ArrayList<>();
        try {
            jsonResponse = jsonResponse.replace("```json", "").replace("```", "").trim();
            aiQuestions = objectMapper.readValue(jsonResponse, new TypeReference<List<Question>>(){});
        } catch (Exception e) {
            e.printStackTrace();
        }

        QuizForm form = new QuizForm();
        form.setQuestions(aiQuestions);
        model.addAttribute("course", course);
        model.addAttribute("quizForm", form);
        return "admin/quiz_preview";
    }

    @PostMapping("/course/{id}/save-quiz")
    public String saveQuiz(@PathVariable Long id, @ModelAttribute QuizForm quizForm) {
        Course course = courseRepository.findById(id).orElseThrow();
        if (quizForm.getQuestions() != null) {
            for (Question q : quizForm.getQuestions()) {
                if (q.getQuestionText() != null && !q.getQuestionText().isEmpty()) {
                    q.setCourse(course);
                    questionRepository.save(q);
                }
            }
        }
        return "redirect:/admin/dashboard";
    }

    // --- COURSE CRUD ---
    @GetMapping("/create-course")
    public String showCreateForm(Model model) { 
        model.addAttribute("course", new Course()); 
        return "admin/course_create"; 
    }

    @PostMapping("/create-course")
    public String createCourse(@ModelAttribute Course course, @RequestParam("file") MultipartFile file) throws IOException {
        if (!file.isEmpty()) { 
            course.setPdfFilename(file.getOriginalFilename()); 
            course.setPdfFile(file.getBytes()); 
        }
        Course savedCourse = courseRepository.save(course); 
        if (savedCourse.getContent() != null && !savedCourse.getContent().isEmpty()) {
            ragService.ingestCourse(savedCourse);
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseRepository.findById(id).orElseThrow()); 
        return "admin/course_edit";
    }

    @PostMapping("/update/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course formCourse) {
        Course existing = courseRepository.findById(id).orElseThrow();
        existing.setTitle(formCourse.getTitle()); 
        existing.setDescription(formCourse.getDescription()); 
        existing.setContent(formCourse.getContent());
        Course savedCourse = courseRepository.save(existing); 
        if (savedCourse.getContent() != null && !savedCourse.getContent().isEmpty()) {
            ragService.ingestCourse(savedCourse);
        }
        return "redirect:/admin/dashboard";
    }

    // --- FIX: ROBUST DELETE COURSE ---
    @Transactional
    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        Course c = courseRepository.findById(id).orElseThrow(); 
        
        // 1. Delete RAG Vectors (The "AI Brain" for this course)
        List<CourseChunk> chunks = courseChunkRepository.findByCourseId(id);
        courseChunkRepository.deleteAll(chunks);

        // 2. Delete Official Questions
        List<Question> questions = questionRepository.findByCourseId(id);
        questionRepository.deleteAll(questions);

        // 3. Delete Student Quiz Results (History)
        // Using stream in case 'findByCourseId' is missing in your specific Repo version
        List<QuizResult> results = quizResultRepository.findAll().stream()
                .filter(r -> r.getCourse().getId().equals(id))
                .collect(Collectors.toList());
        quizResultRepository.deleteAll(results);

        // 4. Clear Student Enrollments (Join Table)
        c.getStudents().clear();
        courseRepository.save(c); 

        // 5. Finally, Delete the Course
        courseRepository.deleteById(id); 
        
        return "redirect:/admin/dashboard";
    }

    // --- STUDENT PROGRESS & DELETE ---
    @GetMapping("/student/{id}/progress")
    public String viewStudentProgress(@PathVariable Long id, Model model) {
        AppUser student = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        List<QuizResult> results = quizResultRepository.findByStudent(student);
        model.addAttribute("student", student);
        model.addAttribute("results", results);
        return "admin/student_details"; 
    }

    @Transactional
    @PostMapping("/student/{id}/delete")
    public String deleteStudent(@PathVariable Long id) {
        AppUser student = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));

        List<QuizResult> results = quizResultRepository.findByStudent(student);
        quizResultRepository.deleteAll(results);

        List<Course> allCourses = courseRepository.findAll();
        for (Course c : allCourses) {
            if (c.getStudents().contains(student)) {
                c.getStudents().remove(student);
                courseRepository.save(c);
            }
        }
        appUserRepository.deleteById(id);
        return "redirect:/admin/students";
    }

    public static class QuizForm {
        private List<Question> questions;
        public List<Question> getQuestions() { return questions; }
        public void setQuestions(List<Question> questions) { this.questions = questions; }
    }
}