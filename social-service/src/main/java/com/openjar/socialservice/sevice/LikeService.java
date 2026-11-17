package com.openjar.socialservice.sevice;

import com.openjar.socialservice.Events.LikeEvent;
import com.openjar.socialservice.config.CurrentUser;
import com.openjar.socialservice.config.RabbitMQConfig;
import com.openjar.socialservice.dto.LikeDto.EmailNotificationDto;
import com.openjar.socialservice.dto.LikeDto.LikeSummaryDto;
import com.openjar.socialservice.dto.RecipeResponseDto;
import com.openjar.socialservice.repository.LikeRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepo likeRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PlatformClient platformClient;

    @Transactional
    public LikeSummaryDto toggleLike(Long recipeId, CurrentUser user) {
        boolean alreadyLiked = likeRepository.existsByUserIdAndRecipeId(user.userId(), recipeId) > 0;

        if (alreadyLiked) {
            likeRepository.deleteByUserIdAndRecipeId(user.userId(), recipeId);
            publishCountChange(recipeId, -1);
            return new LikeSummaryDto(likeRepository.countByRecipeId(recipeId), false);
        }

        Optional<RecipeResponseDto> recipe = platformClient.findRecipe(recipeId); // 404s if missing
        likeRepository.insertLike(user.userId(), recipeId);
        publishCountChange(recipeId, +1);
        recipe.ifPresent(r -> notifyOwner(r, user));
        return new LikeSummaryDto(likeRepository.countByRecipeId(recipeId), true);
    }

    public LikeSummaryDto getSummary(Long recipeId, String userIdOrNull) {
        long total = likeRepository.countByRecipeId(recipeId);
        boolean liked = userIdOrNull != null && likeRepository.existsByUserIdAndRecipeId(userIdOrNull, recipeId) > 0;
        return new LikeSummaryDto(total, liked);
    }

    private void publishCountChange(Long recipeId, int delta) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.OPENJAR_EXCHANGE, RabbitMQConfig.LIKE_ROUTING_KEY,
                    new LikeEvent(recipeId, delta));
        } catch (Exception e) {
            log.error("Could not publish like event for recipe {}: {}", recipeId, e.getMessage());
        }
    }

    /** Best effort: never fails the like itself. */
    private void notifyOwner(RecipeResponseDto recipe, CurrentUser likedBy) {
        if (recipe.getUserId() == null || recipe.getUserId().equals(likedBy.userId())) {
            return;
        }
        platformClient.findUserWithEmail(recipe.getUserId()).ifPresent(owner -> {
            try {
                HashMap<String, Object> model = new HashMap<>();
                model.put("recipeName", recipe.getRecipeName());
                model.put("userName", owner.getUserName());
                model.put("likeeName", likedBy.userName());

                rabbitTemplate.convertAndSend(RabbitMQConfig.OPENJAR_EXCHANGE, RabbitMQConfig.LIKE_Recipe_ROUTING_KEY,
                        new EmailNotificationDto(owner.getUserEmail(),
                                likedBy.userName() + " liked your recipe " + recipe.getRecipeName(),
                                "like-created", model));
            } catch (Exception e) {
                log.error("Could not queue like notification for recipe {}: {}", recipe.getRecipeId(), e.getMessage());
            }
        });
    }
}
