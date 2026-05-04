package com.openjar.recommendation.services;

import com.openjar.recommendation.dto.RecipeSyncMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RecipeSyncConsumer {

    private final VectorStore vectorStore;

    public RecipeSyncConsumer(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @RabbitListener(queues = "recipe.sync.queue")
    public void syncToPinecone(RecipeSyncMessage message) {
        System.out.println("Received recipe for AI sync: " + message.recipeName());

        // 1. Create the text representation of the recipe
        String aiContent = String.format(
            "Recipe Name: %s. Category: %s. Ingredients: %s.",
            message.recipeName(), 
            message.category(), 
            message.ingredients()
        );

        // 2. Attach the MySQL ID as metadata so we can find it later
        Document document = new Document(
            aiContent, 
            Map.of("recipeId", message.recipeId())
        );

        // 3. Save to Pinecone
        vectorStore.add(List.of(document));

        System.out.println("Successfully saved vector to Pinecone for ID: " + message.recipeId());
    }
}