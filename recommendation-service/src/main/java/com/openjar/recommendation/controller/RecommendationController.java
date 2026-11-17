package com.openjar.recommendation.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Semantic recipe search. Reached only through the gateway, which requires login
 * (each query costs paid embedding API calls).
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private static final int MAX_QUERY_LENGTH = 200;

    private final VectorStore vectorStore;

    public RecommendationController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> getRecommendations(@RequestParam String query) {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty() || q.length() > MAX_QUERY_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Query must be between 1 and " + MAX_QUERY_LENGTH + " characters");
        }

        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder().query(q).topK(5).build());

        return similarDocuments.stream().map(Document::getMetadata).toList();
    }
}
