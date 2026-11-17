package com.openjar.socialservice.sevice;

import com.openjar.socialservice.config.CurrentUser;
import com.openjar.socialservice.config.RabbitMQConfig;
import com.openjar.socialservice.dto.CommentResponseDto;
import com.openjar.socialservice.dto.EmailNotificationDto;
import com.openjar.socialservice.dto.RecipeResponseDto;
import com.openjar.socialservice.models.Comment;
import com.openjar.socialservice.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PlatformClient platformClient;

    @Value("${openjar.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public CommentResponseDto postComment(Long recipeId, String content, CurrentUser author) {
        Optional<RecipeResponseDto> recipe = platformClient.findRecipe(recipeId); // 404s if it doesn't exist
        Comment saved = save(recipeId, null, content, author);
        recipe.ifPresent(r -> notifyRecipeOwner(r, author, content));
        return toDto(saved);
    }

    @Override
    public CommentResponseDto postReply(Long recipeId, String parentId, String content, CurrentUser author) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!recipeId.equals(parent.getRecipeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reply must belong to the same recipe");
        }
        if (parent.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reply to a deleted comment");
        }
        return toDto(save(recipeId, parentId, content, author));
    }

    @Override
    public List<CommentResponseDto> getCommentsByRecipe(Long recipeId) {
        List<CommentResponseDto> all = commentRepository.findByRecipeIdOrderByCreatedAtAsc(recipeId)
                .stream().map(this::toDto).toList();

        Map<String, CommentResponseDto> byId = new HashMap<>();
        all.forEach(dto -> byId.put(dto.getId(), dto));

        List<CommentResponseDto> roots = new ArrayList<>();
        for (CommentResponseDto dto : all) {
            CommentResponseDto parent = dto.getParentId() == null ? null : byId.get(dto.getParentId());
            if (parent != null) {
                parent.getReplies().add(dto);
            } else if (dto.getParentId() == null) {
                roots.add(dto);
            }
        }
        return roots;
    }

    @Override
    @Transactional
    public void deleteComment(String commentId, CurrentUser requester) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getUserId().equals(requester.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
        }
        comment.setDeleted(true);   // soft delete keeps reply threads intact
        commentRepository.save(comment);
    }

    // ---------------------------------------------------------------------------------------

    private Comment save(Long recipeId, String parentId, String content, CurrentUser author) {
        return commentRepository.save(Comment.builder()
                .recipeId(recipeId)
                .parentId(parentId)
                .userId(author.userId())
                .userName(author.userName())
                .content(content.trim())
                .build());
    }

    /** Best effort: a failure here must never lose the user's comment. */
    private void notifyRecipeOwner(RecipeResponseDto recipe, CurrentUser author, String content) {
        if (recipe.getUserId() == null || recipe.getUserId().equals(author.userId())) {
            return; // don't email people about their own comments
        }
        platformClient.findUserWithEmail(recipe.getUserId()).ifPresent(owner -> {
            try {
                Map<String, Object> model = new HashMap<>();
                model.put("userName", owner.getUserName());
                model.put("recipeTitle", recipe.getRecipeName());
                model.put("commentContent", content);
                model.put("commenterName", author.userName());
                model.put("recipeUrl", frontendUrl + "/recipe/" + recipe.getRecipeId());
                model.put("recipeImageUrl", recipe.getRecipeImageUrl());

                rabbitTemplate.convertAndSend(RabbitMQConfig.OPENJAR_EXCHANGE, RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                        new EmailNotificationDto(owner.getUserEmail(),
                                "New comment on " + recipe.getRecipeName(), "comment-notification", model));
            } catch (Exception e) {
                log.error("Could not queue comment notification for recipe {}: {}", recipe.getRecipeId(), e.getMessage());
            }
        });
    }

    private CommentResponseDto toDto(Comment c) {
        return CommentResponseDto.builder()
                .id(c.getCommentId())
                .recipeId(String.valueOf(c.getRecipeId()))
                .userId(c.isDeleted() ? null : c.getUserId())
                .userName(c.isDeleted() ? null : c.getUserName())
                .content(c.isDeleted() ? "This comment was deleted." : c.getContent())
                .isDeleted(c.isDeleted())
                .createdAt(c.getCreatedAt())
                .parentId(c.getParentId())
                .replies(new ArrayList<>())
                .build();
    }
}
