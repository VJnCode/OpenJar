package com.openjar.recipe_service.Service;

import com.openjar.recipe_service.Configuration.RabbitMQConfig;
import com.openjar.recipe_service.Event.LikeEvent;

import com.openjar.recipe_service.Repository.RecipeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class RecipeEventListener {

    private final RecipeRepo recipeRepo;

    @RabbitListener(queues = RabbitMQConfig.LIKE_QUEUE) // ← change this
    public void handleLikeToggled(LikeEvent event) {
        recipeRepo.updateLikeCount(event.getRecipeId(), event.getDelta());
    }
}
