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


    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public List<Double> getEmbedding(String text) {
        String url = baseUrl + "text-embedding-004:embedContent?key=" + apiKey;

        try {  
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


    public String generateContent(String prompt) {

        String url = baseUrl + "gemini-2.5-flash:generateContent?key=" + apiKey;

        try {
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

            return "[]"; 
        }
    }

    private String parseGeminiTextResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
            
            return text.replace("```json", "").replace("```", "").trim();
        } catch (Exception e) {
            System.err.println("Error parsing Gemini response: " + e.getMessage());
            return "[]";
        }
    }
}