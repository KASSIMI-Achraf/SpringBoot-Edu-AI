package com.ensamai.pedagogy.dto;

import com.fasterxml.jackson.annotation.JsonAlias; 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class QuizQuestion {
    private int id;
    

    @JsonAlias("question") 
    private String questionText;
    
    private List<String> options;
    private String correctAnswer;


    public QuizQuestion() {
    }


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