package com.ensamai.pedagogy.service;

import com.ensamai.pedagogy.model.Question;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}") 
    private String baseUrl; 
    // Ensure application.properties is: 
    // gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * RAG STEP 1: EMBEDDINGS (Fixed JSON Structure)
     */
    public List<Double> getEmbedding(String text) {
        String url = baseUrl + "text-embedding-004:embedContent?key=" + apiKey;

        try {
            // FIXED: Construct the exact JSON structure Gemini demands
            // { "content": { "parts": [ { "text": "..." } ] } }
            
            ObjectNode rootNode = objectMapper.createObjectNode();
            ObjectNode contentNode = objectMapper.createObjectNode();
            ArrayNode partsArray = objectMapper.createArrayNode();
            ObjectNode textPart = objectMapper.createObjectNode();
            
            textPart.put("text", text);
            partsArray.add(textPart);
            
            contentNode.set("parts", partsArray);
            rootNode.set("content", contentNode);
            rootNode.put("model", "models/text-embedding-004");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(rootNode.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode valuesNode = root.path("embedding").path("values");
                
                List<Double> embedding = new ArrayList<>();
                if (valuesNode.isArray()) {
                    for (JsonNode val : valuesNode) {
                        embedding.add(val.asDouble());
                    }
                }
                return embedding;
            }
        } catch (Exception e) {
            System.err.println("Error fetching embedding: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * RAG STEP 2: GENERATION (Fixed Model Name)
     */
    public String generateContent(String prompt) {
        // FIXED: Changed 'gemini-1.5-flash' to 'gemini-1.5-flash-001'
        // If this still fails, try 'gemini-pro'
        String url = baseUrl + "gemini-2.5-flash:generateContent?key=" + apiKey;

        try {
            // Build JSON: { "contents": [{ "parts": [{ "text": "..." }] }] }
            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode contentsArray = rootNode.putArray("contents");
            ObjectNode contentObj = contentsArray.addObject();
            ArrayNode partsArray = contentObj.putArray("parts");
            partsArray.addObject().put("text", prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(rootNode.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return parseGeminiTextResponse(response.getBody());

        } catch (Exception e) {
            System.err.println("Error generating content: " + e.getMessage());
            // Return valid empty JSON array to prevent Controller crash
            return "[]"; 
        }
    }

    private String parseGeminiTextResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
            
            // Clean up Markdown code blocks
            return text.replace("```json", "").replace("```", "").trim();
        } catch (Exception e) {
            System.err.println("Error parsing Gemini response: " + e.getMessage());
            return "[]";
        }
    }
}