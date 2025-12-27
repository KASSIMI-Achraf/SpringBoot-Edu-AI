package com.ensamai.pedagogy.service;

import com.ensamai.pedagogy.dto.QuizQuestion;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class MockQuizService {

    /**
     * Simulates the RAG + LLM process.
     * In a real app, this would take the 'courseContent' and send it to an AI.
     * Here, we just return hardcoded questions from the PDF.
     */
    public List<QuizQuestion> generateQuiz(String courseContent) {
        // We ignore the input text for now and return the static questions
        return Arrays.asList(
            new QuizQuestion(
                1,
                "What is the main role of Spring Boot?",
                Arrays.asList("A. Database management", "B. Auto-configuration of Spring applications", "C. Frontend rendering", "D. Network communication"),
                "B"
            ),
            new QuizQuestion(
                2,
                "Which annotation defines a REST controller?",
                Arrays.asList("A. @Service", "B. @Repository", "C. @Controller", "D. @RestController"),
                "D"
            ),
             new QuizQuestion(
                3,
                "How do you inject a dependency in Spring?",
                Arrays.asList("A. @Inject", "B. @Autowired", "C. @Bean", "D. @Component"),
                "B"
            )
        );
    }
}