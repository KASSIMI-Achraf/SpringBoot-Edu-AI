package com.ensamai.pedagogy.service;

import com.ensamai.pedagogy.model.Course;
import com.ensamai.pedagogy.model.QuizResult;
import com.ensamai.pedagogy.repository.CourseRepository;
import com.ensamai.pedagogy.repository.QuizResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAgentService {

    private final QuizResultRepository quizResultRepository;
    private final CourseRepository courseRepository;
    private final RagService ragService;
    private final GeminiService geminiService;

    public AiAgentService(QuizResultRepository quizResultRepository,
                          CourseRepository courseRepository,
                          RagService ragService,
                          GeminiService geminiService) {
        this.quizResultRepository = quizResultRepository;
        this.courseRepository = courseRepository;
        this.ragService = ragService;
        this.geminiService = geminiService;
    }

    /**
     * THE AGENTIC WORKFLOW:
     * 1. Analyze History -> 2. Determine Difficulty -> 3. Fetch Context (RAG) -> 4. Generate Quiz
     */
    public String generateQuiz(Long studentId, Long courseId) {
        
        // --- STEP 1: OBSERVE (Analyze Student History) ---
        List<QuizResult> history = quizResultRepository.findByStudentIdAndCourseId(studentId, courseId);
        
        // --- STEP 2: DECIDE (Determine Difficulty) ---
        String difficulty = "EASY"; // Default for beginners
        String pedagogicalGoal = "Focus on basic definitions and core concepts.";
        Integer  n = 5;

        if (!history.isEmpty()) {
            // Get the score of the most recent quiz
            QuizResult lastResult = history.get(history.size() - 1);
            int lastScore = lastResult.getScore();

            if (lastScore < 50) {
                difficulty = "EASY";
                n = 5;
                pedagogicalGoal = "The student failed the last attempt. Generate simpler questions to reinforce basics and build confidence.";
            } else if (lastScore >= 50 && lastScore < 80) {
                difficulty = "MEDIUM";
                n = 10;
                pedagogicalGoal = "The student has a grasp of basics. Introduce application-based questions and slightly more complex scenarios.";
            } else {
                difficulty = "HARD";
                n = 15;
                pedagogicalGoal = "The student is proficient. Challenge them with complex, edge-case, or multi-step reasoning questions.";
            }
        }

        // --- STEP 3: ACT (Retrieve Context via RAG) ---
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        
        // We query the RAG system for concepts relevant to this difficulty
        // (e.g., if Hard, we might search for 'advanced implications')
        String ragQuery = "Core concepts of " + course.getTitle();
        String context = ragService.retrieveContext(course, ragQuery);

        // --- STEP 4: GENERATE (Prompt Engineering) ---
        // We inject the Pedagogical Goal and Strict JSON format into the prompt
        String prompt = String.format("""
            You are an AI Tutor Agent.
            
            CONTEXT FROM COURSE MATERIAL:
            %s
            
            STUDENT STATUS:
            - Difficulty Level: %s
            - Goal: %s
            
            TASK:
            Generate a unique quiz with %d multiple-choice questions based ONLY on the provided context.
            
            OUTPUT FORMAT:
            Return strictly a raw JSON array (no markdown, no ```json wrappers).
            Each object must strictly follow this structure:
            [
              {
                "question": "Question text here?",
                "options": ["A. Option 1", "B. Option 2", "C. Option 3", "D. Option 4"],
                "correctAnswer": "A",
                "explanation": "Brief explanation of why A is correct."
              }
            ]
            """, context, difficulty, pedagogicalGoal,n);

        // Call LLM
        return geminiService.generateContent(prompt);
    }
}