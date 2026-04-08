package com.openjar.socialservice.sevice;

import com.openjar.socialservice.dto.*;
import com.openjar.socialservice.models.Comment;
import com.openjar.socialservice.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
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
        log.info("Processing top-level comment for Recipe: {}", requestDto.getRecipeId());

        String generatedId = UUID.randomUUID().toString();
        Long recipeId = Long.parseLong(requestDto.getRecipeId());

        try {
            commentRepository.insertCommentNative(
                    generatedId,
                    recipeId,
                    requestDto.getUserId(),
                    requestDto.getContent(),
                    null
            );

            // Fetch Recipe Details to get the Owner's Info and Title
            RecipeResponseDto recipe = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8080/recipe/{id}", recipeId)
                    .retrieve()
                    .bodyToMono(RecipeResponseDto.class)
                    .block();


            if (recipe != null && recipe.getUserId() != null) {
                UserResponseDto userProfile = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8086/api/users/{id}", recipe.getUserId())
                        .retrieve()
                        .bodyToMono(UserResponseDto.class)
                        .block();

                Map<String, Object> model = new HashMap<>();

                String displayName = (userProfile != null) ? userProfile.getUserName() : "Chef";
                String targetEmail = (userProfile != null) ? userProfile.getUserEmail() : "varunraj9790@gmail.com";

                model.put("userName", displayName);
                model.put("recipeTitle", recipe.getRecipeName());
                model.put("commentContent", requestDto.getContent());
                model.put("recipeUrl", "http://localhost:3000/recipes/" + recipeId);
                model.put("recipeImageUrl", recipe.getRecipeImageUrl());

                EmailNotificationDto notification = new EmailNotificationDto(
                        targetEmail,
                        "New Comment on " + recipe.getRecipeName(),
                        "comment-notification",
                        model
                );

                rabbitTemplate.convertAndSend("openjar_exchange", "notification_routing_key", notification);
                log.info("Notification sent to {} for recipe {}", displayName, recipe.getRecipeName());
            }
        } catch (Exception e) {
            log.error("Transaction failed: {}", e.getMessage());
            throw new RuntimeException("Flow failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void postReply(CommentRequestDto dto, String recipeIdStr, String parentId, String userId) {
        log.info("User {} is replying to parent comment {}", userId, parentId);

        String newReplyId = UUID.randomUUID().toString();
        Long recipeId = Long.parseLong(recipeIdStr);

        int rows = commentRepository.insertCommentNative(
                newReplyId,
                recipeId,
                userId,
                dto.getContent(),
                parentId
        );

        if (rows == 0) {
            throw new RuntimeException("Could not post reply. Check parent ID.");
        }
    }

    @Override
    public List<CommentResponseDto> getCommentsByRecipe(String recipeIdStr) {
        Long recipeId = Long.parseLong(recipeIdStr);
        log.info("Building comment tree for Recipe: {}", recipeId);

        List<Comment> allComments = commentRepository.findByRecipeIdNative(recipeId);

        List<CommentResponseDto> allDtos = allComments.stream()
                .map(comment -> CommentResponseDto.builder()
                        .id(comment.getCommentId())
                        .recipeId(String.valueOf(comment.getRecipeId()))
                        .userId(comment.getUserId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .parentId(comment.getParentId())
                        .replies(new ArrayList<>())
                        .build())
                .collect(Collectors.toList());

        Map<String, CommentResponseDto> dtoMap = allDtos.stream()
                .collect(Collectors.toMap(CommentResponseDto::getId, dto -> dto));

        List<CommentResponseDto> rootComments = new ArrayList<>();

        for (CommentResponseDto dto : allDtos) {
            if (dto.getParentId() == null) {
                rootComments.add(dto);
            } else {
                CommentResponseDto parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getReplies().add(dto);
                }
            }
        }

        return rootComments;
    }
}