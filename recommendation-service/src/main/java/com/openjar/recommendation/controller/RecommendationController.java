package com.openjar.recommendation.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final VectorStore vectorStore;

    public RecommendationController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> getRecommendations(@RequestParam String query) {
        System.out.println("Searching Pinecone for: " + query);

        // 1. The AI Search (Updated for Spring AI 1.1.4 Builder Syntax)
        List<Document> similarDocuments = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(5)
                .build()
        );

        // 2. Extract the Results
        return similarDocuments.stream()
            .map(Document::getMetadata)
            .collect(Collectors.toList());
    }
}