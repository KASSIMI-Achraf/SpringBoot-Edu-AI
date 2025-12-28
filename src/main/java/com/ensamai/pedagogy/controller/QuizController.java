package com.ensamai.pedagogy.controller;

import com.ensamai.pedagogy.dto.QuizQuestion; 
import com.ensamai.pedagogy.model.*;
import com.ensamai.pedagogy.repository.*;
import com.ensamai.pedagogy.service.AiAgentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
@PreAuthorize("hasAuthority('STUDENT')")
public class QuizController {

    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuestionRepository questionRepository;
    private final AiAgentService aiAgentService;
    private final ObjectMapper objectMapper;

    public QuizController(CourseRepository courseRepository, AppUserRepository appUserRepository,
                          QuizResultRepository quizResultRepository, QuestionRepository questionRepository,
                          AiAgentService aiAgentService) {
        this.courseRepository = courseRepository;
        this.appUserRepository = appUserRepository;
        this.quizResultRepository = quizResultRepository;
        this.questionRepository = questionRepository;
        this.aiAgentService = aiAgentService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @GetMapping("/{courseId}")
    public String quizLanding(@PathVariable Long courseId, Model model) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + courseId));
        List<Question> officialQuestions = questionRepository.findByCourseId(courseId);
        model.addAttribute("course", course);
        model.addAttribute("hasOfficialQuiz", !officialQuestions.isEmpty());
        return "student/quiz_landing";
    }

    @GetMapping("/{courseId}/take")
    public String takeQuiz(@PathVariable Long courseId, 
                           @RequestParam(defaultValue = "ai") String type, 
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        
        Course course = courseRepository.findById(courseId).orElseThrow();
        AppUser student = appUserRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Question> questions = new ArrayList<>();

        if ("official".equals(type)) {
            questions = questionRepository.findByCourseId(courseId);
        } else {
            String jsonQuiz = aiAgentService.generateQuiz(student.getId(), courseId);
            System.out.println("RAW AI RESPONSE: " + jsonQuiz);

            try {
                int startIndex = jsonQuiz.indexOf("[");
                int endIndex = jsonQuiz.lastIndexOf("]");
                
                if (startIndex != -1 && endIndex != -1) {
                    String cleanJson = jsonQuiz.substring(startIndex, endIndex + 1);

                    List<QuizQuestion> dtos = objectMapper.readValue(cleanJson, new TypeReference<List<QuizQuestion>>(){});

                    for (QuizQuestion dto : dtos) {
                        Question q = new Question();
                        q.setQuestionText(dto.getQuestionText());
                        q.setCorrectAnswer(dto.getCorrectAnswer());

                        // Map List<String> to OptionA, OptionB...
                        if (dto.getOptions() != null && dto.getOptions().size() >= 4) {
                            q.setOptionA(cleanOption(dto.getOptions().get(0)));
                            q.setOptionB(cleanOption(dto.getOptions().get(1)));
                            q.setOptionC(cleanOption(dto.getOptions().get(2)));
                            q.setOptionD(cleanOption(dto.getOptions().get(3)));
                        }
                        questions.add(q);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing AI Quiz: " + e.getMessage());
                e.printStackTrace();
            }
        }

        model.addAttribute("questions", questions);
        model.addAttribute("course", course);
        model.addAttribute("quizType", type);
        return "student/quiz_view";
    }

    private String cleanOption(String opt) {
        return opt.replaceAll("^[A-D]\\.\\s*", ""); 
    }

    @PostMapping("/submit")
    public String submitQuiz(@RequestParam Long courseId, 
                             @RequestParam Map<String, String> allParams,
                             @AuthenticationPrincipal UserDetails userDetails, 
                             Model model) {
        
        Course course = courseRepository.findById(courseId).orElseThrow();
        AppUser student = appUserRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        int correctCount = 0;
        int totalQuestions = 0;
        List<ResultDetail> results = new ArrayList<>();

        for (String key : allParams.keySet()) {
            if (key.startsWith("answer_")) {
                String index = key.replace("answer_", "");
                String userAnswer = allParams.get(key);
                String correctAnswer = allParams.get("correct_" + index);
                String questionText = allParams.get("text_" + index);
                String explanation = allParams.getOrDefault("explanation_" + index, "");

                if (correctAnswer != null) {
                    totalQuestions++;
                    if (userAnswer != null && userAnswer.equalsIgnoreCase(correctAnswer)) {
                        correctCount++;
                        results.add(new ResultDetail(questionText, userAnswer, correctAnswer, true, explanation));
                    } else {
                        results.add(new ResultDetail(questionText, userAnswer, correctAnswer, false, explanation));
                    }
                }
            }
        }

        int finalScore = totalQuestions > 0 ? (int) (((double) correctCount / totalQuestions) * 100) : 0;
        quizResultRepository.save(new QuizResult(student, course, finalScore, correctCount, totalQuestions));

        model.addAttribute("score", finalScore);
        model.addAttribute("total", totalQuestions);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("passed", finalScore >= 50);
        model.addAttribute("results", results);
        return "student/quiz_result";
    }

    public static class ResultDetail {
        public String questionText;
        public String userAnswer;
        public String correctAnswer;
        public boolean isCorrect;
        public String explanation;

        public ResultDetail(String q, String u, String c, boolean i, String e) {
            this.questionText = q;
            this.userAnswer = u;
            this.correctAnswer = c;
            this.isCorrect = i;
            this.explanation = e;
        }
    }
}