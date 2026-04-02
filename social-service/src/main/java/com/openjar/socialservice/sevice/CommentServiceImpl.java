package com.openjar.socialservice.sevice;


import com.openjar.socialservice.dto.CommentRequestDto;
import com.openjar.socialservice.dto.CommentResponseDto;
import com.openjar.socialservice.dto.RecipeNotificationDto;
import com.openjar.socialservice.dto.RecipeResponseDto;
import com.openjar.socialservice.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient.Builder webClientBuilder;

    @Override
    @Transactional
    public void postComment(CommentRequestDto requestDto) {
        log.info("Processing comment for Recipe: {}...", requestDto.getRecipeId());

        String generatedId = java.util.UUID.randomUUID().toString();

        try {
            commentRepository.insertCommentNative(
                    generatedId,
                    requestDto.getRecipeId(),
                    requestDto.getUserId(),
                    requestDto.getContent()
            );

            RecipeResponseDto recipe = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8080/recipe/{id}", requestDto.getRecipeId())
                    .retrieve()
                    .bodyToMono(RecipeResponseDto.class)
                    .block();

            if (recipe != null && recipe.getUserId() != null) {
                log.info("Recipe Owner: {}", recipe.getUserId());

                RecipeNotificationDto notification = new RecipeNotificationDto(
                        "varunraj9790@gmail.com",
                        "New Comment Alert",
                        "Someone commented on your recipe: " + requestDto.getContent()
                );

                rabbitTemplate.convertAndSend("openjar_exchange", "notification_routing_key", notification);
                log.info("Notification queued.");
            }
        } catch (Exception e) {
            log.error("Flow Error: {}", e.getMessage());
            throw new RuntimeException("Flow failed");
        }
    }

    @Override
    public List<CommentResponseDto> getCommentsByRecipe(String recipeId) {
        return commentRepository.findByRecipeIdNative(recipeId).stream()
                .map(comment -> CommentResponseDto.builder()
                        .id(comment.getCommentId())
                        .recipeId(comment.getRecipeId())
                        .userId(comment.getUserId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}