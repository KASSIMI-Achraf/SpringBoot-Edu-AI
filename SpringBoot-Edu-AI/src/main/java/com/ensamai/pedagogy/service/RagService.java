package com.ensamai.pedagogy.service;

import com.ensamai.pedagogy.model.Course;
import com.ensamai.pedagogy.model.CourseChunk;
import com.ensamai.pedagogy.repository.CourseChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private CourseChunkRepository chunkRepository;

    // 1. INGESTION: Breaks text into chunks and saves vectors
    public void ingestCourse(Course course) {
        // Simple chunking by regex splitting (e.g., by double newlines or periods)
        // In production, use a library like LangChain4j
        String[] rawChunks = course.getContent().split("(?<=\\.)\\s+"); 
        
        // Group sentences into larger chunks (approx 500 chars)
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        for (String sentence : rawChunks) {
            if (currentChunk.length() + sentence.length() > 500) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(sentence).append(" ");
        }
        if (!currentChunk.isEmpty()) chunks.add(currentChunk.toString());

        // Process and Save
        for (String text : chunks) {
            List<Double> vector = geminiService.getEmbedding(text);
            CourseChunk chunk = new CourseChunk();
            chunk.setContent(text);
            chunk.setEmbedding(vector);
            chunk.setCourse(course);
            chunkRepository.save(chunk);
        }
    }

    // 2. RETRIEVAL: Finds the most relevant content for a query
    public String retrieveContext(Course course, String query) {
        List<Double> queryVector = geminiService.getEmbedding(query);
        List<CourseChunk> allChunks = chunkRepository.findByCourseId(course.getId());

        // Find the top 3 most similar chunks
        return allChunks.stream()
                .sorted(Comparator.comparingDouble(c -> -cosineSimilarity(c.getEmbedding(), queryVector))) // Descending
                .limit(3)	
                .map(CourseChunk::getContent)
                .collect(Collectors.joining("\n---\n"));
    }

    // Math helper: Calculate similarity between two vectors
    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1 == null || v2 == null || v1.size() != v2.size()) return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            normA += Math.pow(v1.get(i), 2);
            normB += Math.pow(v2.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}