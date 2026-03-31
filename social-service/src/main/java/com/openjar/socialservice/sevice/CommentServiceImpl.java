package com.openjar.socialservice.sevice;

import com.openjar.socialservice.Repository.CommentRepository;
import com.openjar.socialservice.dto.CommentRequestDto;
import com.openjar.socialservice.dto.CommentResponseDto;
import com.openjar.socialservice.models.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void postComment(CommentRequestDto requestDto) {
        String newId = java.util.UUID.randomUUID().toString();

        commentRepository.insertCommentNative(
                newId,
                requestDto.getRecipeId(),
                requestDto.getUserId(),
                requestDto.getContent()
        );

        Map<String, String> emailData = new HashMap<>();
        emailData.put("recipientEmail", "varun@example.com"); // Hardcoded for now
        emailData.put("subject", "New Comment on your Recipe!");
        emailData.put("messageBody", "User " + requestDto.getUserId() + " commented: " + requestDto.getContent());

        try {
            rabbitTemplate.convertAndSend("notification_exchange", "notification_routing_key", emailData);
            log.info("Successfully queued notification for comment ID: {}", newId);
        } catch (Exception e) {
            log.error("RabbitMQ Down: Comment saved, but notification failed: {}", e.getMessage());
        }
    }

    @Override
    public List<CommentResponseDto> getCommentsByRecipe(String recipeId) {
        return commentRepository.findByRecipeIdNative(recipeId).stream()
                .map(comment -> CommentResponseDto.builder()
                        .id(comment.getId()) // String to String - perfect!
                        .recipeId(comment.getRecipeId())
                        .userId(comment.getUserId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();
    }
}