package com.ensamai.pedagogy.dto;

import com.fasterxml.jackson.annotation.JsonAlias; // Import this
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Import this
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignores "id" if AI doesn't send it
public class QuizQuestion {
    private int id;
    
    // EXPLANATION: The AI sends "question", but we want "questionText".
    // @JsonAlias tells Jackson: "If you see 'question' in JSON, put it here."
    @JsonAlias("question") 
    private String questionText;
    
    private List<String> options;
    private String correctAnswer;

    // --- 1. MANDATORY: No-Args Constructor for Jackson Parsing ---
    public QuizQuestion() {
    }

    // Existing Constructor
    public QuizQuestion(int id, String questionText, List<String> options, String correctAnswer) {
        this.id = id;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
}