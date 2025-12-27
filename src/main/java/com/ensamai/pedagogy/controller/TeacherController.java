package com.ensamai.pedagogy.controller;

import com.ensamai.pedagogy.model.*;
import com.ensamai.pedagogy.repository.*;
import com.ensamai.pedagogy.service.GeminiService;
import com.ensamai.pedagogy.service.RagService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

/**
 * Controller for teacher functionality (course management, student enrollment, quiz generation)
 * Both ADMIN and TEACHER roles can access these endpoints
 */
@Controller
@RequestMapping("/teacher")
@PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
public class TeacherController {

    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuestionRepository questionRepository;
    private final CourseChunkRepository courseChunkRepository;
    private final GeminiService geminiService;
    private final RagService ragService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public TeacherController(CourseRepository courseRepository, AppUserRepository appUserRepository,
                           QuizResultRepository quizResultRepository, QuestionRepository questionRepository,
                           CourseChunkRepository courseChunkRepository,
                           GeminiService geminiService, RagService ragService, 
                           PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.courseRepository = courseRepository;
        this.appUserRepository = appUserRepository;
        this.quizResultRepository = quizResultRepository;
        this.questionRepository = questionRepository;
        this.courseChunkRepository = courseChunkRepository;
        this.geminiService = geminiService;
        this.ragService = ragService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    /**
     * Get current teacher from authentication
     */
    private AppUser getCurrentTeacher(Authentication auth) {
        return appUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Check if current user is admin (can see all courses)
     */
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
    }

    /**
     * Get courses - admin sees all, teacher sees only their own
     */
    private List<Course> getAccessibleCourses(Authentication auth) {
        if (isAdmin(auth)) {
            return courseRepository.findAll();
        } else {
            AppUser teacher = getCurrentTeacher(auth);
            return courseRepository.findByTeacherId(teacher.getId());
        }
    }

    /**
     * Helper to redirect to the correct dashboard based on role
     */
    private String getDashboardRedirect(Authentication auth) {
        return isAdmin(auth) ? "redirect:/admin/courses" : "redirect:/teacher/dashboard";
    }

    // --- DASHBOARD ---
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("courses", getAccessibleCourses(auth));
        model.addAttribute("isAdmin", isAdmin(auth));
        return "teacher/dashboard";
    }

    // --- ENROLLMENT ---
    @GetMapping("/enroll/{id}")
    public String showEnrollForm(@PathVariable Long id, Model model, Authentication auth) {
        Course course = courseRepository.findById(id).orElseThrow();
        
        // Permission check: only owner or admin can enroll
        if (!isAdmin(auth) && !course.getTeacher().getId().equals(getCurrentTeacher(auth).getId())) {
            return "redirect:/teacher/dashboard?error=access_denied";
        }
        
        List<AppUser> students = appUserRepository.findByRole(Role.STUDENT);
        model.addAttribute("course", course);
        model.addAttribute("students", students);
        return "teacher/enroll";
    }

    @PostMapping("/enroll")
    public String enrollStudent(@RequestParam Long courseId, @RequestParam Long studentId, Authentication auth) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        
        // Permission check
        if (!isAdmin(auth) && !course.getTeacher().getId().equals(getCurrentTeacher(auth).getId())) {
            return "redirect:/teacher/dashboard?error=access_denied";
        }
        
        AppUser student = appUserRepository.findById(studentId).orElseThrow();
        course.enrollStudent(student);
        courseRepository.save(course);
        return "redirect:/teacher/dashboard";
    }

    // --- STUDENTS ---
    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", appUserRepository.findByRole(Role.STUDENT));
        return "teacher/student_list"; 
    }

    @GetMapping("/create-student")
    public String showCreateStudentForm(Model model) {
        model.addAttribute("student", new AppUser());
        return "teacher/student_create";
    }

    @PostMapping("/create-student")
    public String createStudent(@ModelAttribute AppUser student) {
        student.setRole(Role.STUDENT);
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        student.setActive(true);
        student.setDeletable(true);
        appUserRepository.save(student);
        return "redirect:/teacher/students";
    }

    // --- QUIZ & AI ---
    @GetMapping("/course/{id}/generate-quiz")
    public String generateQuizPreview(@PathVariable Long id, Model model, Authentication auth) {
        Course course = courseRepository.findById(id).orElseThrow();
        
        // Permission check
        if (!isAdmin(auth) && (course.getTeacher() == null || !course.getTeacher().getId().equals(getCurrentTeacher(auth).getId()))) {
            return "redirect:/teacher/dashboard?error=access_denied";
        }
        
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
        return "teacher/quiz_preview";
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
        return getDashboardRedirect(auth);
    }

    // --- COURSE CRUD ---
    @GetMapping("/create-course")
    public String showCreateForm(Model model) { 
        model.addAttribute("course", new Course()); 
        return "teacher/course_create"; 
    }

    @PostMapping("/create-course")
    public String createCourse(@ModelAttribute Course course, @RequestParam("file") MultipartFile file, 
                               Authentication auth) throws IOException {
        // Set the current teacher as the owner
        AppUser teacher = getCurrentTeacher(auth);
        course.setTeacher(teacher);
        
        if (!file.isEmpty()) { 
            course.setPdfFilename(file.getOriginalFilename()); 
            course.setPdfFile(file.getBytes()); 
        }
        Course savedCourse = courseRepository.save(course); 
        if (savedCourse.getContent() != null && !savedCourse.getContent().isEmpty()) {
            ragService.ingestCourse(savedCourse);
        }
        return getDashboardRedirect(auth);
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication auth) {
        Course course = courseRepository.findById(id).orElseThrow();
        
        // Permission check
        if (!isAdmin(auth) && (course.getTeacher() == null || !course.getTeacher().getId().equals(getCurrentTeacher(auth).getId()))) {
            return "redirect:/teacher/dashboard?error=access_denied";
        }
        
        model.addAttribute("course", course); 
        return "teacher/course_edit";
    }

    @PostMapping("/update/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course formCourse, Authentication auth) {
        Course existing = courseRepository.findById(id).orElseThrow();
        
        // Permission check
        if (!isAdmin(auth) && (existing.getTeacher() == null || !existing.getTeacher().getId().equals(getCurrentTeacher(auth).getId()))) {
            return "redirect:/teacher/dashboard?error=access_denied";
        }
        
        existing.setTitle(formCourse.getTitle()); 
        existing.setDescription(formCourse.getDescription()); 
        existing.setContent(formCourse.getContent());
        Course savedCourse = courseRepository.save(existing); 
        if (savedCourse.getContent() != null && !savedCourse.getContent().isEmpty()) {
            ragService.ingestCourse(savedCourse);
        }
        return getDashboardRedirect(auth);
    }

    // --- DELETE COURSE ---
    @Transactional
    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id, Authentication auth) {
        Course c = courseRepository.findById(id).orElseThrow();
        
        // Permission check
        if (!isAdmin(auth) && (c.getTeacher() == null || !c.getTeacher().getId().equals(getCurrentTeacher(auth).getId()))) {
            return "redirect:/teacher/dashboard?error=access_denied";
        }
        
        // 1. Delete RAG Vectors
        List<CourseChunk> chunks = courseChunkRepository.findByCourseId(id);
        courseChunkRepository.deleteAll(chunks);

        // 2. Delete Official Questions
        List<Question> questions = questionRepository.findByCourseId(id);
        questionRepository.deleteAll(questions);

        // 3. Delete Student Quiz Results
        List<QuizResult> results = quizResultRepository.findAll().stream()
                .filter(r -> r.getCourse().getId().equals(id))
                .collect(Collectors.toList());
        quizResultRepository.deleteAll(results);

        // 4. Clear Student Enrollments
        c.getStudents().clear();
        courseRepository.save(c); 

        // 5. Delete the Course
        courseRepository.deleteById(id); 
        
        return getDashboardRedirect(auth);
    }

    // --- STUDENT PROGRESS & DELETE ---
    @GetMapping("/student/{id}/progress")
    public String viewStudentProgress(@PathVariable Long id, Model model) {
        AppUser student = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        List<QuizResult> results = quizResultRepository.findByStudent(student);
        model.addAttribute("student", student);
        model.addAttribute("results", results);
        return "teacher/student_details"; 
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
        return "redirect:/teacher/students";
    }

    public static class QuizForm {
        private List<Question> questions;
        public List<Question> getQuestions() { return questions; }
        public void setQuestions(List<Question> questions) { this.questions = questions; }
    }
}
